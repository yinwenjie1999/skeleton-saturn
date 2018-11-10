package com.vanda.platform.saturn.core.scan;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.engine.annotation.BuildCustomRepository;
import com.vanda.platform.saturn.core.engine.annotation.SaturnEntity;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnUpdateMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.OrderType;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.QueryType;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentProperty;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod;
import com.vanda.platform.saturn.core.model.PersistentRelation;
import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

/**
 * 基于Javassist的class字节码分析工具，目前已知的分析套件包括了JPA系列标签和自有系列标签<br>
 * 该类以及其子类基于模板模式进行实现
 * @author yinwenjie
 */
public abstract class JavassistAnalysis {
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JavassistAnalysis.class);
  
  /**
   * 进行语义分析，得到实体定义模型的结构化表达<br>
   * 该接口有两个不同实现——基于JPA标签的和完全基于Saturn自有标签的
   * @param currentCtClass 当前要分析的
   * @param properties 当前已完成分析的普通字段信息（递归使用）
   * @param relations 当前已完成分析的关联性字段信息（递归使用）
   * @param isChildPersistent 标记当前类的子类是否已经确定是一个PersistentClass，如果是说明本次递归即使父级类没有(因为本接口的实现支持递归)
   * @return
   */
  public PersistentClass analyze(CtClass currentCtClass , List<PersistentProperty> properties , List<PersistentRelation> relations , boolean isChildPersistent) {
    /*
     * 分析一个满足模型定义的类和其所有父级类中的字段定义信息，该方法支持递归性的字段搜索
     * 操作过程如下：
     * 1、首先确定这个类是一个满足模型定义的结构类，也就是说要求Entity或者SaturnEntity注解
     * 2、处理过程是从其可能的最顶级父类开始进行，所以最先处理的是其可能的父类，
     * 其中用来存储所有普通字段定义的集合和关联性字段定义的集合是需要带入递归中复用的
     * 3、处理基本信息，注意只记录最底层子类的信息，父类的基本信息不用记录、
     * 4、随后分析这个类（以及该类所有父类）中的一般性字段，也就是为基础类型、原生类型、日期类型这样的字段
     * 5、最后分析这个类（以及该类所有父类）中的关联性字段，也就是为单泛型集合、其它满足模型定义要求的关联类
     * */
    /*
     * 分析一个满足模型定义的类和其所有父级类中的字段定义信息，该方法支持递归性的字段搜索
     * 操作过程如下：
     * 1、首先确定这个类是一个满足模型定义的结构类，也就是说要求Entity或者SaturnEntity注解
     * 2、处理过程是从其可能的最顶级父类开始进行，所以最先处理的是其可能的父类，
     * 其中用来存储所有普通字段定义的集合和关联性字段定义的集合是需要带入递归中复用的
     * 3、处理基本信息，注意只记录最底层子类的信息，父类的基本信息不用记录、
     * 4、随后分析这个类（以及该类所有父类）中的一般性字段，也就是为基础类型、原生类型、日期类型这样的字段
     * 5、最后分析这个类（以及该类所有父类）中的关联性字段，也就是为单泛型集合、其它满足模型定义要求的关联类
     * */
    // 1、=========================
    if(currentCtClass == null) {
      return null;
    }
    PersistentClass prsistentClass = null;
    if(!isChildPersistent) {
      prsistentClass = new PersistentClass();
    }
    // 验证类的关键标记信息
    // 如果当前处理过程上一次递归的子类并不是persistentClass，
    // 并且又没有找到JPA的关键Entity信息或者又没有找到SaturnEntity信息，则不再需要处理这个类
    boolean hasEntityAnnotation = currentCtClass.hasAnnotation(Entity.class);
    boolean hasSaturnEntityAnnotation = currentCtClass.hasAnnotation(SaturnEntity.class);
    if(!hasEntityAnnotation && !hasSaturnEntityAnnotation && !isChildPersistent) {
      return null;
    }
    if(hasEntityAnnotation && currentCtClass.hasAnnotation(Table.class)) {
      try {
        Table tableAnnotation = (Table)currentCtClass.getAnnotation(Table.class);
        prsistentClass.setRepositoryTable(tableAnnotation.name());
      } catch (ClassNotFoundException e) {
        LOGGER.error(e.getMessage() , e);
        return null;
      }
    }
    
    // 2、=========================
    // 最先分析的是其父类 ,properties和relations用于递归性记录本类及它所有父类的一般性字段和关联性字段
    if(properties == null) {
      properties = new LinkedList<>();
    }
    if(relations == null) {
      relations = new LinkedList<>();
    }
    CtClass superClass = null;
    try {
      superClass = currentCtClass.getSuperclass();
    } catch (NotFoundException e) {
      LOGGER.warn(e.getMessage());
    }
    // 如果存在父类，则递归进行属性检测
    if(superClass != null) {
      analyze(superClass, properties , relations , true);
    }
    
    // 3、=========================
    // 以下是基本信息，注意，如果当前递归正在对父类进行分析，那么就不需要记录类的基本信息了
    String className = currentCtClass.getName();
    if(prsistentClass != null) {
      String pkage = currentCtClass.getPackageName();
      String simpleName = currentCtClass.getSimpleName();
      boolean hasBuildCustomRepositoryAnnotation = currentCtClass.hasAnnotation(BuildCustomRepository.class);
      String domain = null;
      if(hasSaturnEntityAnnotation) {
        SaturnEntity saturnEntityAnnotation = null;
        try {
          saturnEntityAnnotation = (SaturnEntity)currentCtClass.getAnnotation(SaturnEntity.class);
        } catch (ClassNotFoundException e) {
          LOGGER.error(e.getMessage() , e);
          return null;
        }
        domain = saturnEntityAnnotation.domain();
      }
      prsistentClass.setRepositoryEntity(hasEntityAnnotation);
      prsistentClass.setBuildCustomRepository(hasBuildCustomRepositoryAnnotation);
      prsistentClass.setSimpleClassName(simpleName);
      prsistentClass.setClassName(className);
      prsistentClass.setDomain(domain);
      prsistentClass.setPkage(pkage);
    }
    
    // 4、=========================分析基础信息类型的属性
    CtField[] fields = currentCtClass.getDeclaredFields();
    // 注意，没有任何属性的可能性是存在的，就是某一个父类中没有定义任何属性
    if(fields == null || fields.length == 0) {
      return prsistentClass;
    }
    int fieldIndex = 0;
    for (CtField fieldItem : fields) {
      LOGGER.debug("fieldName == " + fieldItem.getName());
      PersistentProperty persistentProperty = this.analysisGeneralField(fieldItem, ++fieldIndex);
      if(persistentProperty != null) {
        persistentProperty.setPersistentClassName(className);
        properties.add(persistentProperty);
      }
    }
    if(prsistentClass != null) {
      prsistentClass.setProperties(properties);
    }
    
    // 5、=========================接着分析可能存在关联的对象
    // 以下是可能存在的各个prsistentClass的关联关系
    // 注意，如果一个关联的class已经在之前被分析过，就不需要再分析了，只需要从已分析过的集合中取出即可
    // 另外，如果一个关联的class是已经递归过的，也就不需要再分析了，只需要建立关联即可
    fieldIndex = 0;
    Class<?> reflectClass = null;
    try {
      reflectClass = Class.forName(className);
    } catch (ClassNotFoundException e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
    for (CtField fieldItem : fields) {
      LOGGER.debug("fieldName == " + fieldItem.getName());
      PersistentRelation relation = analysisRelationField(reflectClass , fieldItem, fieldIndex);
      if(relation != null) {
        relation.setPersistentClassName(className);
        relations.add(relation);
      }
    }
    if(prsistentClass != null) {
      prsistentClass.setRelations(relations);
    }
    
    return prsistentClass;
  }
  
  /**
   * 分析一个满足模型字段规范的定义信息——一般性对象<br>
   * 此方法中的实现内容再基于JPA标签进行分析时，和基于自有标签进行分析时，实现过程是不一样的
   * @param fieldItem 当前要进行分析的字段信息
   * @param fieldIndex 当前字段的定义序号
   * @return 分析后的结果将形成PersistentProperty对象
   */
  protected abstract PersistentProperty analysisGeneralField(CtField fieldItem , int fieldIndex);
  
  /**
   * 分析一个满足模型字段规范的定义信息———关联性对象<br>
   * 此方法中的实现内容再基于JPA标签进行分析时，和基于自有标签进行分析时，实现过程是不一样的
   * @param reflectClass 当前class的反射类型表达，这主要是为了取得其中泛型的类型
   * @param fieldItem 当前要进行分析的字段信息
   * @param fieldIndex 当前字段的定义序号
   * @return 分析后的结果将形成PersistentRelation对象
   */
  protected abstract PersistentRelation analysisRelationField(Class<?> reflectClass , CtField fieldItem , int fieldIndex);
  
  /**
   * TODO 很重要的方法，但是注释没有写
   * @param prsistentClass
   * @param saturnUpdateMethod
   * @return 
   */
  public PersistentUpdateMethod analysisUpdateMethod(PersistentClass prsistentClass , SaturnUpdateMethod saturnUpdateMethod) {
    /*
     * 检验和处理过程为：
     * TODO 继续写
     * */
    // 首先是方法的描述信息，没有这个分析程序就会报错
    PersistentUpdateMethod persistentUpdateMethod = new PersistentUpdateMethod();
    String className = prsistentClass.getClassName();
    String description = saturnUpdateMethod.description();
    Validate.notBlank(description , "自定义修改方法中的描述信息必须指定[" + className + ":UpdateMethod]!");
    persistentUpdateMethod.setDescription(description);
    persistentUpdateMethod.setPersistentClassName(className);
    String methodName = saturnUpdateMethod.methodName();
    Validate.isTrue(StringUtils.startsWithAny(methodName, "update") , "自定义更新方法只能以update关键字开始!!");
    persistentUpdateMethod.setMethodName(methodName);
    
    // 然后时对查询条件中的设定进行校验和处理
    // 不可能没有查询条件，因为没有查询条件就是全范围数据更新，这肯定是错误的
    // TODO 这段代码重用度非常高，需要合并成一个私有方法
    String queryFields[] = saturnUpdateMethod.queryParams();
    Validate.isTrue(queryFields != null && queryFields.length > 0 , "必须设定更新方法的查询条件，请检查!!");
    List<PersistentProperty> persistentPropertys = prsistentClass.getProperties();
    Map<String, PersistentProperty> persistentPropertyMapping = 
        persistentPropertys.stream().collect(Collectors.toMap(PersistentProperty::getPropertyName, persistentProperty -> persistentProperty));
    for (String queryFieldItem : queryFields) {
      Validate.isTrue(StringUtils.indexOf(queryFieldItem, '.') == -1, "查询条件不支持关联特性“.”");
      PersistentProperty persistentProperty = persistentPropertyMapping.get(queryFieldItem);
      Validate.notNull(persistentProperty , "未发现查询条件配置中指定的属性" + queryFieldItem + "，请检查!");
    }
    persistentUpdateMethod.setQueryParams(queryFields);
    
    // 最后对更新字段中的配置进行校验和处理
    // 不可能没有更新字段的
    // TODO 这段代码重用度非常高，需要合并成一个私有方法
    String updateFields[] = saturnUpdateMethod.updateParams();
    Validate.isTrue(updateFields != null && updateFields.length > 0 , "必须设定更新方法的更新字段，请检查!!");
    for (String updateFieldItem : updateFields) {
      Validate.isTrue(StringUtils.indexOf(updateFieldItem, '.') == -1, "更新字段不支持关联特性“.”");
      PersistentProperty persistentProperty = persistentPropertyMapping.get(updateFieldItem);
      Validate.notNull(persistentProperty , "未发现更新字段配置中指定的属性" + updateFieldItem + "，请检查!");
    }
    persistentUpdateMethod.setUpdateParams(updateFields);
    
    return persistentUpdateMethod;
  }
  
  /**
   * 在当前的模型描述结构中，查找指定的属性。注意属性是支持级联的，例如：user.roles.connect;
   * @param prsistentClass 
   * @param paramArrayItems 当前正在被处理的关联性字段信息，例如user.roles.connect;
   * @param itemIndex 
   */
  private PersistentProperty foundRelationParms(PersistentClass prsistentClass , String paramArrayItems[] , int itemIndex , Map<String, PersistentClass> persistentClassMapping) {
    Validate.notNull(prsistentClass , "没有找到模型结构描述!!");
    Validate.isTrue(paramArrayItems != null && paramArrayItems.length > 0, "没有找到模型结构中的指定属性!!");
    
    // 为了查找方便，先使用stream将list变成map
    List<PersistentProperty> persistentPropertys = prsistentClass.getProperties();
    Validate.isTrue(persistentPropertys != null && !persistentPropertys.isEmpty() , "没有找到模型结构中的指定属性!!");
    List<PersistentRelation> persistentRelations = prsistentClass.getRelations();
    Validate.isTrue(persistentRelations != null && !persistentRelations.isEmpty() , "没有找到模型结构中的指定属性!!");
    Map<String, PersistentProperty> persistentPropertyMapping = 
        persistentPropertys.stream().collect(Collectors.toMap(PersistentProperty::getPropertyName, persistentProperty -> persistentProperty));
    Map<String, PersistentRelation> persistentRelationMapping =  
        persistentRelations.stream().collect(Collectors.toMap(PersistentRelation::getPropertyName, persistentRelation -> persistentRelation));
    
    // 开始查询
    String currentParamItem = paramArrayItems[itemIndex];
    PersistentProperty currentPersistentProperty = null;
    PersistentProperty persistentProperty = persistentPropertyMapping.get(currentParamItem);
    
    // 如果条件成立，说明是在普通属性集合中找到了指定的属性，这很重要，因为普通属性不可能再向下遍历了
    if(persistentProperty != null) {
      currentPersistentProperty = persistentProperty;
      // 这种情况已经不可能再向下一级递归了，那么如果当前不是最后一级，则抛出异常
      Validate.isTrue(itemIndex + 1 == paramArrayItems.length , "没有找到模型结构中的指定属性!!");
    } else {
      PersistentRelation persistentRelation = persistentRelationMapping.get(currentParamItem);
      Validate.notNull(persistentRelation , "没有找到模型结构中的指定属性!!");
      
      // 如果条件成立，说明可以且应该向深度进行遍历
      if(itemIndex + 1 < paramArrayItems.length) {
        String propertyClass = persistentRelation.getPropertyClass();
        PersistentClass nextPersistentClass = persistentClassMapping.get(propertyClass);
        Validate.notNull(nextPersistentClass , "没有找到指定的模型结构(一旦出现这个错误，可能是之前第一次模型扫描出现了bug，请联系程序员)!!");
        currentPersistentProperty = foundRelationParms(nextPersistentClass, paramArrayItems, itemIndex + 1 , persistentClassMapping);
      } 
      // 如果条件成立，说明是最后一级，在关联性属性中找到最后一级，说明不满足查找要求
      // 因为查找要求是最后一级必须是普通属性，所以这里就应该抛出异常
      else if(itemIndex + 1 == paramArrayItems.length) {
        throw new IllegalArgumentException("根据配置要求，最后一级属性必须是一般类型的属性，请检查查询字段的配置!!"); 
      }
    }
    
    return currentPersistentProperty;
  }

  /**
   * TODO 很重要的方法，注释也要写
   * @param properties 该模型扫描完成后，已经存在的一般属性
   * @param queryMethodAnnotation 当前描述的自定义查询注解
   * @return 
   */
  public PersistentQueryMethod analysisQueryMethod(PersistentClass prsistentClass , SaturnQueryMethod queryMethodAnnotation , Map<String, PersistentClass> persistentClassMapping) {
    /*
     * 处理过程为：
     * 1、首选处理自定义方法的描述信息，自定义方法的描述信息是必须的，如果没有就会忽略这个方法
     * 2、然后处理查询条件，查询条件支持关联性属性，例如：user.roles.connect。但最后一个属性必须是一般性属性
     * 3、处理查询类型，查询类型必须和查询条件一一对应，如果两者数量不一致，则需要进行补足
     * 4、注意，排序字段和排序类型都不是必须的，所以如果排序字段没有设定，就不需要再继续下去了。
     * 5、最后处理排序类型，排序类型的处理和第“3”步，查询类型的处理类似，请参考
     * */
    PersistentQueryMethod persistentQueryMethod = new PersistentQueryMethod();
    String className = prsistentClass.getClassName();
    persistentQueryMethod.setPersistentClassName(className);
    String description = queryMethodAnnotation.description();
    Validate.notBlank(description , "自定义查询方法中的描述信息必须指定[" + className + ":QueryMethod]!");
    persistentQueryMethod.setDescription(description);
    String methodName = queryMethodAnnotation.methodName();
    Validate.isTrue(StringUtils.startsWithAny(methodName, "query","find") , "自定义查询方法只能以query或者find开始!!");
    persistentQueryMethod.setMethodName(methodName);
    
    // 对查询条件属性进行验证，由于自定义查询支持多级属性，所以这里要对属性嵌套进行验证
    String params[] = queryMethodAnnotation.params();
    if(params == null || params.length == 0) {
      return null;
    }
    for (String param : params) {
      String[] paramArrayItems = StringUtils.split(param, ".");
      PersistentProperty persistentProperty = foundRelationParms(prsistentClass, paramArrayItems , 0 , persistentClassMapping);
      Validate.notNull(persistentProperty , "没有发现自定义查询中的属性，或者属性不符合配置要求：[" + param + "]");
    }
    persistentQueryMethod.setParams(params);
    
    // 对条件类型进行验证，有多少个属性，就需要有多少个查询类型，如果没有指定默认填补EQUAL
    QueryType[] queryTypeAnnotations = queryMethodAnnotation.queryType();
    // 进行填充
    PersistentQueryMethod.QueryType[] currentQueryTypes = new PersistentQueryMethod.QueryType[params.length];
    Arrays.fill(currentQueryTypes, PersistentQueryMethod.QueryType.EQUAL);
    for(int index = 0 ; queryTypeAnnotations != null && index < queryTypeAnnotations.length && index < params.length ; index++) {
      QueryType queryTypeItem = queryTypeAnnotations[index];
      String param = params[index];
      Validate.notBlank(param , "查询字段信息不能为null，或者空字符串，请检查!!");
      if(queryTypeItem == QueryType.BETWEEN) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.BETWEEN;
      } else if(queryTypeItem == QueryType.EQUAL) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.EQUAL;
      } else if(queryTypeItem == QueryType.GREATEREQUALTHAN) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.GREATEREQUALTHAN;
      } else if(queryTypeItem == QueryType.GREATERTHAN) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.GREATERTHAN;
      } else if(queryTypeItem == QueryType.LESSEQUALTHAN) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.LESSEQUALTHAN;
      } else if(queryTypeItem == QueryType.LESSTHAN) {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.LESSTHAN;
      } else {
        currentQueryTypes[index] = PersistentQueryMethod.QueryType.EQUAL;
      }
    }
    persistentQueryMethod.setQueryTypes(currentQueryTypes);
    
    // 对排序条件进行验证和处理
    String orderParams[] =  queryMethodAnnotation.orderByParams();
    // 如果条件成立，后续对排序配置的分析就没有必要继续了
    if(orderParams == null || orderParams.length == 0 || 
        (orderParams.length == 1 && StringUtils.equals("", orderParams[0]))) {
      return persistentQueryMethod; 
    }
    // 注意，排序字段只能来自于被模型中的字段，所以不支持“.”，如果出现“.”则报告异常
    List<PersistentProperty> persistentPropertys = prsistentClass.getProperties();
    Map<String, PersistentProperty> persistentPropertyMapping = 
        persistentPropertys.stream().collect(Collectors.toMap(PersistentProperty::getPropertyName, persistentProperty -> persistentProperty));
    for (String orderParamItem : orderParams) {
      Validate.isTrue(StringUtils.indexOf(orderParamItem, '.') == -1, "排序属性不支持关联特性“.”");
      PersistentProperty persistentProperty = persistentPropertyMapping.get(orderParamItem);
      Validate.notNull(persistentProperty , "未发现排序配置中指定的属性" + orderParamItem + "，请检查!");
    }
    persistentQueryMethod.setOrderByParams(orderParams);
    
    // 对排序类型进行验证和处理——主要是检查和处理排序方式和排序字段的相关性
    OrderType[] orderTypeAnnotations = queryMethodAnnotation.orderType();
    PersistentQueryMethod.OrderType[] currentOrderTypes = new PersistentQueryMethod.OrderType[orderParams.length];
    Arrays.fill(currentOrderTypes, PersistentQueryMethod.OrderType.ASC);
    for(int index = 0 ; orderTypeAnnotations != null && index < orderTypeAnnotations.length && index < orderParams.length ; index++) {
      OrderType orderTypeItem = orderTypeAnnotations[index];
      String orderParam = orderParams[index];
      Validate.notBlank(orderParam , "排序字段信息不能为null，或者空字符串，请检查!!");
      if(orderTypeItem == OrderType.ASC) {
        currentOrderTypes[index] = PersistentQueryMethod.OrderType.ASC;
      } else {
        currentOrderTypes[index] = PersistentQueryMethod.OrderType.DESC;
      }
    }
    persistentQueryMethod.setOrderTypes(currentOrderTypes);
    
    return persistentQueryMethod;
  }
}