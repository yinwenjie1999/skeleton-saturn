package com.vanda.platform.saturn.core.test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.engine.annotation.BuildCustomRepository;
import com.vanda.platform.saturn.core.engine.annotation.BuildQueryMethods;
import com.vanda.platform.saturn.core.engine.annotation.BuildUpdateMethods;
import com.vanda.platform.saturn.core.engine.annotation.SaturnColumn;
import com.vanda.platform.saturn.core.engine.annotation.SaturnColumnRelation;
import com.vanda.platform.saturn.core.engine.annotation.SaturnEntity;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.OrderType;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.QueryType;
import com.vanda.platform.saturn.core.engine.annotation.SaturnUpdateMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnValidate;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentProperty;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod;
import com.vanda.platform.saturn.core.model.PersistentRelation;
import com.vanda.platform.saturn.core.model.PersistentRelation.RelationType;
import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtPrimitiveType;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

/**
 * 可单独运行，对Javassist相关使用进行测试
 * @author yinwenjie
 */
public class TestJavassistScan {
  
  private ClassPool classPool;
  private static final Logger LOGGER = LoggerFactory.getLogger(TestJavassistScan.class);
  private Map<String, PersistentClass> persistentClassMapping = new LinkedHashMap<>();
  
  @Before
  public void standby() {
    classPool = ClassPool.getDefault();
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    classPool.appendClassPath(new LoaderClassPath(currentClassLoader));
  }
  
  @Test
  public void handleClass() {
    String rootPackage = "com.vanda.platform.saturn.core.test";
//    String rootPackage = "com.vanda.platform.im21.pojo";
    String rootPackagePath = StringUtils.replaceAll(rootPackage, "\\.", "/");
    
    /*
     * 过程为：
     * 1、基于当前rootPackage，获得当前所有的url信息，这是第一级遍历的基础
     * 2、
     * */
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> urls = null;
    try {
      urls = currentClassLoader.getResources(rootPackagePath);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      Assert.assertFalse(true);
    }
    
    // 开始遍历，对最终是要对所有符合模型规范的class文件的属性进行分析
    List<Class<?>> classes = new LinkedList<>();
    while(urls.hasMoreElements()) {
      URL classUrl = urls.nextElement();
      LOGGER.info("classUrl = " + classUrl);
      String protocol = classUrl.getProtocol();
      // 如果是以文件的形式保存在服务器上
      if ("file".equals(protocol)) {
        analysisFilePath(classes , classUrl.getPath() , rootPackage);
      } 
      // 否则就是jar包形式了
      else if("jar".equals(protocol)) {
        analysisJarPath(classes , classUrl);
      } 
    }
    
    // 然后再进行一次遍历，这次遍历是要对符合模型规范的class文件的方法定义进行分析
    Collection<PersistentClass> persistentClasses = this.persistentClassMapping.values();
    for (PersistentClass persistentClass : persistentClasses) {
      String className = persistentClass.getClassName();
      CtClass currentCtClass;
      try {
        currentCtClass = this.classPool.get(className);
      } catch (NotFoundException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException(e.getMessage());
      }
      analysisClassForMethods(currentCtClass, persistentClass);
    }
  }
  
  /**
   * TODO 注释未写，但是有很重要，所以必须写
   * @param classes
   * @param jarUrl
   */
  private void analysisJarPath(List<Class<?>> classes ,URL jarUrl) {
    JarFile jar;
    try {
      jar = ((JarURLConnection) jarUrl.openConnection()).getJarFile();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return;
    }
    Enumeration<JarEntry> entries = jar.entries();
    while(entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      String jarEntryName = jarEntry.getName();
      // 如果条件成立，说明这个文件不是可能需要的class
      if(!StringUtils.endsWith(jarEntryName, ".class") || StringUtils.contains(jarEntryName, "$")) {
        continue;
      }
      String classFullName = StringUtils.removeEnd(jarEntryName, ".class");
      classFullName = StringUtils.replaceAll(classFullName, "/", ".");
      
      // 可以加载class了
      CtClass currentCtClass = null;
      try {
        currentCtClass = classPool.get(classFullName);
      } catch (NotFoundException e) {
        LOGGER.error(e.getLocalizedMessage());
        continue;
      }
      PersistentClass persistentClass = analysisClassForFields(null , null , currentCtClass , false);
      if(persistentClass != null) {
        this.persistentClassMapping.put(classFullName, persistentClass);
      }
    }
  }
  
  /**
   * TODO 注释未写，但是有很重要，所以必须写
   * @param classes
   * @param currentPackagePath
   * @param currentPackageName
   */
  private void analysisFilePath(List<Class<?>> classes , String currentPackagePath , String currentPackageName) {
    File dir = new File(currentPackagePath);
    // 如果不存在或者 也不是目录就直接返回
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    File[] childfiles = dir.listFiles((File pathname) -> {
      return (pathname.isDirectory()) || (pathname.getName().endsWith(".class"));
    });
    
    for (File file : childfiles) {
      // 如果是目录 则继续扫描
      if (file.isDirectory()) {
        String childPackageName = file.getName();
        analysisFilePath(classes , file.getAbsolutePath() , currentPackageName + "." + childPackageName);
      } else {
        String fileName = file.getName();
        // 只有class文件才符合处理条件，否则跳过即可
        if(!file.getName().endsWith(".class")) return;
        String fileSortName = StringUtils.removeEnd(fileName, ".class");
        CtClass currentCtClass = null;
        String className = currentPackageName + "." + fileSortName;
        try {
          currentCtClass = classPool.get(className);
        } catch (NotFoundException e) {
          LOGGER.error(e.getLocalizedMessage());
          continue;
        }
        PersistentClass persistentClass = analysisClassForFields(null , null , currentCtClass , false);
        if(persistentClass != null) {
          this.persistentClassMapping.put(className, persistentClass);
        }
      }
    }
  }
  
  /**
   * 分析满足模型定义规范的class中的字段信息，具体过程参见方法内部的说明信息
   * @param properties 
   * @param relations 
   * @param currentCtClass 
   * @param isChildPersistent 标记当前类的子类是否已经确定是一个PersistentClass，如果是说明本次递归即使父级类没有
   */
  private PersistentClass analysisClassForFields(List<PersistentProperty> properties , List<PersistentRelation> relations , CtClass currentCtClass , boolean isChildPersistent) {
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
      analysisClassForFields(properties , relations , superClass, true);
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
      PersistentProperty persistentProperty = this.scanGeneralField(fieldItem, ++fieldIndex);
      if(persistentProperty != null) {
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
      PersistentRelation relation = scanRelationField(reflectClass , fieldItem, fieldIndex);
      if(relation != null) {
        relations.add(relation);
      }
    }
    if(prsistentClass != null) {
      prsistentClass.setRelations(relations);
    }
    
    // 最终返回
    return prsistentClass;
  }
  
  /**
   * 分析一个满足模型字段规范的定义信息——一般性对象
   * @param fieldItem
   * @param fieldIndex
   * @return
   */
  private PersistentProperty scanGeneralField(CtField fieldItem , int fieldIndex) {
    // 在分析一般属性时，只有具有SaturnColumn注解或者Column注解的属性才有分析的意义
    boolean hasSaturnColumnAnnotation = fieldItem.hasAnnotation(SaturnColumn.class);
    boolean hasColumnAnnotation = fieldItem.hasAnnotation(Column.class);
    boolean hasPrimaryKeyAnnotation = fieldItem.hasAnnotation(Id.class);
    if(!hasSaturnColumnAnnotation && !hasColumnAnnotation && !hasPrimaryKeyAnnotation) {
      return null;
    } 
    
    CtClass fieldType = null;
    // 基本类型只有int long byte short float double
    // java.lang.*、java.util.Date、java.math.*
    String fieldTypeName = null;
    try {
      fieldType = fieldItem.getType();
      // 如果条件成立，说明是一个原始类型
      if(fieldType instanceof CtPrimitiveType) {
        CtPrimitiveType primitiveType = (CtPrimitiveType)fieldType;
        fieldTypeName = primitiveType.getWrapperName();
      } else {
        fieldTypeName = fieldType.getName();
      }
    } catch (NotFoundException e) {
      LOGGER.debug(e.getMessage());
      return null;
    }
    
    // 如果条件成立，说明不是基础类型，不需要在这里进行处理
    if(!StringUtils.startsWith(fieldTypeName, "java.lang.")
        && !StringUtils.startsWith(fieldTypeName, "java.math.")
        && !StringUtils.equals(fieldTypeName, "java.util.Date")) {
      return null;
    }
    
    /*
     * 注意1：如果当前class是一个jpa形式的class，那么以JPA注解的设定为主，以Saturn注解的设定为辅
     * （如果有重复的，则以Saturn相关注解覆盖之）
     * 注意2：如果当前class不是一个JPA形式的class，那么以Saturn注解为准
     * 
     * 对于普通字段的分析过程如下：
     * 1、分析可能存在的主键信息
     * 2、分析可能存在的Column信息
     * 3、分析可能存在的SaturnColumn信息
     * */
    String fieldName = fieldItem.getName();
    PersistentProperty property = new PersistentProperty();
    property.setPropertyClass(fieldTypeName);
    property.setPropertyName(fieldName);
    property.setIndex(fieldIndex);
    // 1、============如果条件成立，说明是主键字段
    if(hasPrimaryKeyAnnotation) {
      property.setPrimaryKey(true);
      property.setCanUpdate(false);
      property.setUnique(true);
      property.setPropertyDbName("id");
      property.setNullable(false);
    }
    property.setPropertyClass(fieldType.getName());
    
    // 2、============
    // 如果条件成立，则首先读取Column中的信息
    if(hasColumnAnnotation) {
      Column columnAnnotation;
      try {
        columnAnnotation = (Column)fieldItem.getAnnotation(Column.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = columnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean canUpdate = columnAnnotation.updatable();
      property.setCanUpdate(canUpdate);
      boolean nullable = columnAnnotation.nullable();
      property.setNullable(nullable);
      String propertyDbName = columnAnnotation.name();
      property.setPropertyDbName(propertyDbName);
      boolean unique = columnAnnotation.unique();
      property.setUnique(unique);
    }
    
    // 3、============
    // 如果条件成立说明有SaturnColumn，这里面的属性有优先权
    // 实际上这里的一些属性，只有当prsistentClass是一个持久层模型定义时才能使用
    // TODO 但这里先不管，先记录设置情况
    if(hasSaturnColumnAnnotation) {
      SaturnColumn saturnColumnAnnotation;
      try {
        saturnColumnAnnotation = (SaturnColumn)fieldItem.getAnnotation(SaturnColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = saturnColumnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean nullable = saturnColumnAnnotation.nullable();
      property.setNullable(nullable);
      boolean pkColumn = saturnColumnAnnotation.pkColumn();
      property.setPrimaryKey(pkColumn);
      boolean unique = saturnColumnAnnotation.unique();
      property.setUnique(unique);
      boolean updatable = saturnColumnAnnotation.updatable();
      property.setCanUpdate(updatable);
      String description = saturnColumnAnnotation.description();
      property.setPropertyDesc(description);
    }
    
    // 可能的Validate信息
    boolean hasSaturnValidateAnnotation = fieldItem.hasAnnotation(SaturnValidate.class);
    if(hasSaturnValidateAnnotation) {
      SaturnValidate saturnValidateAnnotation;
      try {
        saturnValidateAnnotation = (SaturnValidate)fieldItem.getAnnotation(SaturnValidate.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      property.setValidateType(saturnValidateAnnotation.type());
    }
    return property;
  }

  /**
   * TODO 很重要的方法，注释也要写
   * @param parentClass
   * @param fieldItem
   * @param fieldIndex
   * @return
   */
  private PersistentRelation scanRelationField(Class<?> reflectClass , CtField fieldItem , int fieldIndex) {
    // 在分析一般属性时，只有具有SaturnColumn注解或者Column注解的属性才有分析的意义
    boolean hasSaturnColumnAnnotation = fieldItem.hasAnnotation(SaturnColumn.class);
    boolean hasJoinColumnAnnotation = fieldItem.hasAnnotation(JoinColumn.class);
    boolean hasManyToOneAnnotation = fieldItem.hasAnnotation(ManyToOne.class);
    boolean hasManyToManyAnnotation = fieldItem.hasAnnotation(ManyToMany.class);
    boolean hasOneToManyAnnotation = fieldItem.hasAnnotation(OneToMany.class);
    boolean hasOneToOneAnnotation = fieldItem.hasAnnotation(OneToOne.class);
    boolean hasSaturnRelationAnnotation = fieldItem.hasAnnotation(SaturnColumnRelation.class);
    if(!hasSaturnColumnAnnotation && !hasJoinColumnAnnotation && !hasManyToOneAnnotation
        && !hasManyToManyAnnotation && !hasOneToManyAnnotation && !hasOneToOneAnnotation
        && !hasSaturnRelationAnnotation) {
      return null;
    } 
    
    CtClass fieldType = null;
    // 关联的信息实际上
    String fieldTypeName = null;
    String fieldName = fieldItem.getName();
    try {
      fieldType = fieldItem.getType();
      // 如果条件成立，说明是一个原始类型，那么就不是关联类型了
      if(fieldType instanceof CtPrimitiveType) {
        return null;
      } else {
        fieldTypeName = fieldType.getName();
      }
    } catch (NotFoundException e) {
      LOGGER.debug(e.getMessage());
      return null;
    }
    
    // 如果条件成立，说明不是基础类型，不需要在这里进行处理
    if(StringUtils.startsWith(fieldTypeName, "java.lang.")
        || StringUtils.startsWith(fieldTypeName, "java.math.")
        || StringUtils.equals(fieldTypeName, "java.util.Date")) {
      return null;
    }
    
    // 如果当前类型是一个单泛型集合，那么取出泛型类型才是真的类型
    boolean isCollectionInterface = false;
    // 如果以下代码判定成功，说明当前字段是一个单泛型集合性质的类
    try {
      CtClass collectionClass = classPool.get("java.util.Collection");
      isCollectionInterface = fieldType.subtypeOf(collectionClass);
      if(isCollectionInterface) {
        // 如果以代码段判定成功，则说明这是一个集合泛型，要以集合中的泛型类型作为字段的类型
        // 并且还需要验证这个泛型类型是否为一个合法的模型定义：既是有Entity或者SaturnEntity
        Field reflectField = reflectClass.getDeclaredField(fieldName);
        Type genericType = reflectField.getGenericType();
        // 如果条件成立才说明在反射类型描述中存在泛型信息
        if(genericType instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) genericType;  
          Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
          fieldTypeName = genericClazz.getName();
        }
      }
    } catch (NoSuchFieldException | SecurityException | NotFoundException e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
    
    // 接下来进行验证这个fieldTypeName是一个符合要求的模型描述，否则也不进行处理了
    CtClass paramsClass;
    try {
      paramsClass = classPool.get(fieldTypeName);
    } catch (NotFoundException e) {
      LOGGER.error(e.getMessage() , e);
      return null;
    }
    boolean hasEntityAnnotation = paramsClass.hasAnnotation(Entity.class);
    boolean hasSaturnEntityAnnotation = paramsClass.hasAnnotation(SaturnEntity.class);
    if(!hasEntityAnnotation && !hasSaturnEntityAnnotation) {
      return null;
    }
    
    // 如果存在JoinColumn，则首先根据其中属性设置属性特性
    PersistentProperty property = new PersistentProperty();
    property.setPropertyClass(fieldTypeName);
    property.setPropertyName(fieldName);
    property.setIndex(fieldIndex);
    if(hasJoinColumnAnnotation) {
      JoinColumn columnAnnotation;
      try {
        columnAnnotation = (JoinColumn)fieldItem.getAnnotation(JoinColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = columnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean canUpdate = columnAnnotation.updatable();
      property.setCanUpdate(canUpdate);
      boolean nullable = columnAnnotation.nullable();
      property.setNullable(nullable);
      String propertyDbName = columnAnnotation.name();
      property.setPropertyDbName(propertyDbName);
      boolean unique = columnAnnotation.unique();
      property.setUnique(unique);
    }
    
    // 如果条件成立说明有SaturnColumn，这里面的属性有优先权
    // 实际上这里的一些属性，只有当prsistentClass是一个持久层模型定义时才能使用
    // TODO 但这里先不管，先记录设置情况
    if(hasSaturnColumnAnnotation) {
      SaturnColumn saturnColumnAnnotation;
      try {
        saturnColumnAnnotation = (SaturnColumn)fieldItem.getAnnotation(SaturnColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = saturnColumnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean nullable = saturnColumnAnnotation.nullable();
      property.setNullable(nullable);
      boolean pkColumn = saturnColumnAnnotation.pkColumn();
      property.setPrimaryKey(pkColumn);
      boolean unique = saturnColumnAnnotation.unique();
      property.setUnique(unique);
      boolean updatable = saturnColumnAnnotation.updatable();
      property.setCanUpdate(updatable);
      String description = saturnColumnAnnotation.description();
      property.setPropertyDesc(description);
    }
    
    // 建立关联
    PersistentRelation relation = new PersistentRelation();
    relation.setProperty(property);
    if(hasManyToOneAnnotation) {
      relation.setRelationType(RelationType.ManyToOne);
    } else if(hasManyToManyAnnotation) {
      relation.setRelationType(RelationType.ManyToMany);
    } else if(hasOneToManyAnnotation) {
      relation.setRelationType(RelationType.OneToMany);
    } else if(hasOneToOneAnnotation) {
      relation.setRelationType(RelationType.OneToOne);
    } else if(hasSaturnRelationAnnotation) {
      SaturnColumnRelation saturnRelationAnnotation;
      try {
        saturnRelationAnnotation = (SaturnColumnRelation)fieldItem.getAnnotation(SaturnColumnRelation.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage() + "：错误的关联类型，请检查建模情况");
        return null;
      }
      SaturnColumnRelation.RelationType pRelationType = saturnRelationAnnotation.type();
      if(pRelationType == SaturnColumnRelation.RelationType.MANYTOMANY) {
        relation.setRelationType(RelationType.ManyToMany);
      } else if(pRelationType == SaturnColumnRelation.RelationType.MANYTOONE) {
        relation.setRelationType(RelationType.ManyToOne);
      } else if(pRelationType == SaturnColumnRelation.RelationType.ONETOMANY) {
        relation.setRelationType(RelationType.OneToMany);
      } else if(pRelationType == SaturnColumnRelation.RelationType.ONETOONE) {
        relation.setRelationType(RelationType.OneToOne);
      }
    } else {
      LOGGER.warn("错误的关联类型，请检查建模情况");
      return null;
    }
    
    return relation;
  }

  /**
   * TODO 很重要的方法，还没有写注释
   * @param currentCtClass
   * @return
   */
  private void analysisClassForMethods(CtClass currentCtClass , PersistentClass persistentClass) {
    // 以下是定制的查询方法的处理过程
    boolean hasBuildQueryMethodsAnnotation = currentCtClass.hasAnnotation(BuildQueryMethods.class);
    List<PersistentQueryMethod> persistentQueryMethods = null;
    if(hasBuildQueryMethodsAnnotation) {
      Object[] methodsAnnotationObjects = null;
      try {
        methodsAnnotationObjects = currentCtClass.getAnnotations();
      } catch (ClassNotFoundException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException(e);
      }
      
      SaturnQueryMethod[] saturnQueryMethods = null;
      for(int index = 0 ; methodsAnnotationObjects != null && index < methodsAnnotationObjects.length ; index++) {
        Object annotationObject = methodsAnnotationObjects[index];
        // 如果条件成立，才说明他是一个自定义查询性质的描述
        if(annotationObject instanceof BuildQueryMethods) {
          BuildQueryMethods buildQueryMethods = (BuildQueryMethods)annotationObject;
          saturnQueryMethods = buildQueryMethods.methods();
          break;
        }
      }
      if(saturnQueryMethods != null) {
        persistentQueryMethods = new LinkedList<>();
        for (SaturnQueryMethod saturnQueryMethod : saturnQueryMethods) {
          PersistentQueryMethod persistentQueryMethod = scanQueryMethod(persistentClass, saturnQueryMethod);
          if(persistentQueryMethod != null) {
            persistentQueryMethods.add(persistentQueryMethod);
          }
        }
      }
    }
    persistentClass.setQueryMethods(persistentQueryMethods);
    
    // 以下是定制的修改方法
    // 注意修改方法中进行写操作的字段只能在本class（或其父类）存在，查询条件也是
    boolean hasBuildUpdateMethodsAnnotation = currentCtClass.hasAnnotation(BuildUpdateMethods.class);
    List<PersistentUpdateMethod> persistentUpdateMethods = null;
    if(hasBuildUpdateMethodsAnnotation) {
      Object[] methodsAnnotationObjects = null;
      try {
        methodsAnnotationObjects = currentCtClass.getAnnotations();
      } catch (ClassNotFoundException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException(e);
      }
      
      SaturnUpdateMethod[] saturnUpdateMethods = null;
      for(int index = 0 ; methodsAnnotationObjects != null && index < methodsAnnotationObjects.length ; index++) {
        Object annotationObject = methodsAnnotationObjects[index];
        // 如果条件成立，才说明他是一个自定义修改性质的描述
        if(annotationObject instanceof BuildUpdateMethods) {
          BuildUpdateMethods buildUpdateMethods = (BuildUpdateMethods)annotationObject;
          saturnUpdateMethods = buildUpdateMethods.methods();
          break;
        }
      }
      if(saturnUpdateMethods != null) {
        persistentUpdateMethods = new LinkedList<>();
        for (SaturnUpdateMethod saturnUpdateMethod : saturnUpdateMethods) {
          PersistentUpdateMethod persistentUpdateMethod = scanUpdateMethod(persistentClass, saturnUpdateMethod);
          if(persistentUpdateMethod != null) {
            persistentUpdateMethods.add(persistentUpdateMethod);
          }
        }
      }
    }
    persistentClass.setUpdateMethods(persistentUpdateMethods);
  }
  
  /**
   * TODO 很重要的方法，但是注释没有写
   * @param prsistentClass
   * @param saturnUpdateMethod
   * @return
   */
  private PersistentUpdateMethod scanUpdateMethod(PersistentClass prsistentClass , SaturnUpdateMethod saturnUpdateMethod) {
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
    persistentUpdateMethod.setQueryParams(updateFields);
    
    return persistentUpdateMethod;
  }
  
  /**
   * TODO 很重要的方法，注释也要写
   * @param properties 该模型扫描完成后，已经存在的一般属性
   * @param relations 该模型扫描完成后，已经存在的关联性属性
   * @param queryMethodAnnotation 当前描述的自定义查询注解
   * @return 
   */
  private PersistentQueryMethod scanQueryMethod(PersistentClass prsistentClass , SaturnQueryMethod queryMethodAnnotation) {
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
    String description = queryMethodAnnotation.description();
    Validate.notBlank(description , "自定义查询方法中的描述信息必须指定[" + className + ":QueryMethod]!");
    persistentQueryMethod.setDescription(description);
    
    // 对查询条件属性进行验证，由于自定义查询支持多级属性，所以这里要对属性嵌套进行验证
    String params[] = queryMethodAnnotation.params();
    if(params == null || params.length == 0) {
      return null;
    }
    for (String param : params) {
      String[] paramArrayItems = StringUtils.split(param, ".");
      PersistentProperty persistentProperty = foundRelationParms(prsistentClass, paramArrayItems , 0);
      Validate.notNull(persistentProperty , "没有发现自定义查询中的属性，或者属性不符合配置要求：[" + param + "]");
    }
    persistentQueryMethod.setParams(params);
    
    // 对条件类型进行验证，有多少个属性，就需要有多少个查询类型，如果没有指定默认填补EQUAL
    QueryType[] queryTypeAnnotations = queryMethodAnnotation.queryType();
    if(queryTypeAnnotations == null || queryTypeAnnotations.length == 0) {
      queryTypeAnnotations = new QueryType[params.length];
    }
    PersistentQueryMethod.QueryType[] currentQueryTypes = null;
    // 进行填充
    currentQueryTypes = new PersistentQueryMethod.QueryType[params.length];
    Arrays.fill(currentQueryTypes, PersistentQueryMethod.QueryType.EQUAL);
    for(int index = 0 ; index < queryTypeAnnotations.length && index < params.length ; index++) {
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
    PersistentQueryMethod.OrderType[] currentOrderTypes = null;
    if(orderTypeAnnotations == null) {
      currentOrderTypes = new PersistentQueryMethod.OrderType[orderParams.length];
    }
    Arrays.fill(currentOrderTypes, PersistentQueryMethod.OrderType.ASC);
    for(int index = 0 ; index < orderTypeAnnotations.length && index < orderParams.length ; index++) {
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

  /**
   * 在当前的模型描述结构中，查找指定的属性。注意属性是支持级联的，例如：user.roles.connect;
   * @param prsistentClass 
   * @param paramArrayItems 当前正在被处理的关联性字段信息，例如user.roles.connect;
   * @param itemIndex 
   */
  private PersistentProperty foundRelationParms(PersistentClass prsistentClass , String paramArrayItems[] , int itemIndex) {
    Validate.notNull(prsistentClass , "没有找到模型结构描述!!");
    Validate.isTrue(paramArrayItems != null && paramArrayItems.length > 0, "没有找到模型结构中的指定属性!!");
    
    // 为了查找方便，先使用stream将list变成map
    List<PersistentProperty> persistentPropertys = prsistentClass.getProperties();
    Validate.isTrue(persistentPropertys != null && !persistentPropertys.isEmpty() , "没有找到模型结构中的指定属性!!");
    List<PersistentRelation> persistentRelations = prsistentClass.getRelations();
    Validate.isTrue(persistentRelations != null && !persistentRelations.isEmpty() , "没有找到模型结构中的指定属性!!");
    Map<String, PersistentProperty> persistentPropertyMapping = 
        persistentPropertys.stream().collect(Collectors.toMap(PersistentProperty::getPropertyName, persistentProperty -> persistentProperty));
    // TODO 这个stream的写法还有一些问题
    Map<String, PersistentRelation> persistentRelationMapping = new HashMap<>();
    
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
      persistentProperty = persistentRelation.getProperty();
      Validate.notNull(persistentProperty , "没有找到模型结构中的指定属性(一旦出现这个错误，可能是之前第一次模型扫描出现了bug，请联系程序员)!!");
      
      // 如果条件成立，说明可以且应该向深度进行遍历
      if(itemIndex + 1 < paramArrayItems.length) {
        String propertyClass = persistentProperty.getPropertyClass();
        PersistentClass nextPersistentClass = this.persistentClassMapping.get(propertyClass);
        Validate.notNull(nextPersistentClass , "没有找到指定的模型结构(一旦出现这个错误，可能是之前第一次模型扫描出现了bug，请联系程序员)!!");
        currentPersistentProperty = foundRelationParms(nextPersistentClass, paramArrayItems, itemIndex + 1);
      } 
      // 如果条件成立，说明是最后一级，在关联性属性中找到最后一级，说明不满足查找要求
      // 因为查找要求是最后一级必须是普通属性，所以这里就应该抛出异常
      else if(itemIndex + 1 == paramArrayItems.length) {
        throw new IllegalArgumentException("根据配置要求，最后一级属性必须是一般类型的属性，请检查查询字段的配置!!"); 
      }
    }
    
    return currentPersistentProperty;
  }
}
