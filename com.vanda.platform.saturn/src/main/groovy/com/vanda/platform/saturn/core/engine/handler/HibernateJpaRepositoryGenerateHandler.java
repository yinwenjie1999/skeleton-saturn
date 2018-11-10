package com.vanda.platform.saturn.core.engine.handler;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.context.service.PersistentClassService;
import com.vanda.platform.saturn.core.context.service.PersistentPropertyService;
import com.vanda.platform.saturn.core.context.service.PersistentRelationService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentProperty;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod.OrderType;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod.QueryType;
import com.vanda.platform.saturn.core.model.PersistentRelation;
import com.vanda.platform.saturn.core.model.PersistentRelation.RelationType;
import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;
import com.vanda.platform.saturn.core.utils.TStringUtils;

/**
 * 继承自RepositoryGenerateHandler的，用于生成数据存储层（hibernate）的简单生成器。也是骨架(土星)V3.0提供的存储层默认构建器<br>
 * 暂时先写字符串，后面再改成JDT
 * @author yinwenjie
 */
public class HibernateJpaRepositoryGenerateHandler extends AbstractJavaFileGenerateHandler implements SaturnHandler {
  
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(HibernateJpaRepositoryGenerateHandler.class);

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.handler.SaturnHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  @Override
  public void handle(SaturnContext context) {
    /*
     * 生成过程为：
     * 1、首先确定配置参数，必须要有该工程的根包信息，只对拥有JPA标记的持久层模型对象进行扫描
     * 如果过滤后发现没有任何持久化对象，则终止处理
     * 2、循环进行模型处理，每一个循环的处理方式如下：
     * 3、每一次循环都需要创建文件内容（具体过程请参见buildJavaFileContext方法），只要文件内容创建成功了，就应该创建这个文件本身了
     * 4、每一次循环还要视当前类模型的定义情况，确定是否需要创建数据层自定文件的内容和文件本身
     * TODO 目前在没有JDT支持的情况下，就是用普通的字符串操作
     * */
    
    // 1、=========
    String rootPackage = context.getRootPackage();
    String projectAbsolutePath = context.getProjectAbsolutePath();
    String projectSrcAbsolutePath = StringUtils.join(projectAbsolutePath , "/" , context.getProjectSrcPath());
    Validate.notBlank(rootPackage , "必须指定根包路径[rootPackages]，才能在根包路径的位置完成数据层代码生成");
    Validate.notBlank(projectSrcAbsolutePath , "必须指定主代码路径[projectSrcAbsolutePath]");
    PersistentClassService persistentClassService = context.getPersistentClassService();
    Validate.notNull(persistentClassService , "not found persistentClassService!!error");
    List<PersistentClass> persistentClasses = persistentClassService.queryAllClasses();
    Validate.isTrue(persistentClasses != null && !persistentClasses.isEmpty() , "未发现任何模型信息，请检查是否前置运行扫描器");
    PersistentPropertyService persistentPropertyService = context.getPersistentPropertyService();
    PersistentRelationService persistentRelationService = context.getPersistentRelationService();
    Validate.notNull(persistentPropertyService , "not found persistentPropertyService!!error");
    Validate.notNull(persistentRelationService , "not found persistentRelationService!!error");
    
    // 2、=========
    List<PersistentClass> entityPersistentClasses = persistentClasses.stream().filter(persistentClass -> persistentClass.getRepositoryEntity())
    .collect(Collectors.toList());
    if(entityPersistentClasses != null && entityPersistentClasses.isEmpty()) {
      LOGGER.warn("未发现基于数据层定义的模型规范，数据层JPA生成过程终止!!");
      return;
    }
    
    // 开始进行循环处理，视情况确认当出现异常时是终止还是继续下一个
    Map<String, String> importMapping = new HashMap<>();
    for (PersistentClass itemPersistentClass : entityPersistentClasses) {
      String simpleClassName = itemPersistentClass.getSimpleClassName();
      String repositoryInternalPackage = StringUtils.join(rootPackage,".repository.internal");
      String repositoryPackage = StringUtils.join(rootPackage , ".repository");
      String repositorySimpleClass = StringUtils.join(simpleClassName , "Repository");
      String customRepositorySimpleClass = StringUtils.join(simpleClassName , "RepositoryCustom");
      String customRepositoryImplSimpleClass = StringUtils.join(simpleClassName , "RepositoryImpl");
      
      try {
        // 3、===========
        StringBuffer repositoryFileContext = this.buildRepositoryFileContext(itemPersistentClass, importMapping, rootPackage, persistentClassService, persistentPropertyService, persistentRelationService);
        if(repositoryFileContext != null) {
          this.buildJavaFile(repositoryFileContext.toString(), projectSrcAbsolutePath, repositoryPackage, repositorySimpleClass);
        }
        
        // 4、===========
        if(itemPersistentClass.getBuildCustomRepository()) {
          StringBuffer customRepositoryContext = this.buildCustomRepositoryFileContext(itemPersistentClass, rootPackage);
          if(customRepositoryContext != null) {
            this.buildJavaFile(customRepositoryContext.toString(), projectSrcAbsolutePath, repositoryInternalPackage, customRepositorySimpleClass);
          }
          StringBuffer customeRepositoryImplContext = this.buildCustomRepositoryImplFileContext(itemPersistentClass, rootPackage);
          if(customeRepositoryImplContext != null) {
            this.buildJavaFile(customeRepositoryImplContext.toString(), projectSrcAbsolutePath, repositoryInternalPackage, customRepositoryImplSimpleClass);
          }
        }
      } catch(Exception e) {
        LOGGER.warn(e.getMessage());
        continue;
      }
    }
  }
  
  private StringBuffer buildCustomRepositoryFileContext(PersistentClass persistentClass , String rootPackage) {
    /*
     * 生成的内容效果示意：
     * package XXXXX.repository.internal;
     * import XXXXXX;
     * 注释自动生成
     * @author saturn
     * public interface AlarmEventRepositoryCustom {
     *   /**
     *    * 这是分页方法的雏形，可以根据业务要求进行修改
     *    * @param pageable
     *    * @param conditions 
     *    *
     *   Page<AlarmEventEntity> queryPage(Pageable pageable , Map<String, Object> conditions);
     * }
     * */
    String persistentClassName = persistentClass.getClassName();
    String simpleClassName = persistentClass.getSimpleClassName();
    String repositoryInternalPackage = StringUtils.join(rootPackage,".repository.internal");
    String customRepositorySimpleClass = StringUtils.join(simpleClassName , "RepositoryCustom");
    
    // 开始生成内容——package和class
    StringBuffer code = new StringBuffer();
    code.append("package ").append(repositoryInternalPackage).append(";").append(System.lineSeparator());
    code.append("import java.util.Map;").append(System.lineSeparator());
    code.append("import org.springframework.data.domain.Page;").append(System.lineSeparator());
    code.append("import org.springframework.data.domain.Pageable;").append(System.lineSeparator());
    code.append("import ").append(persistentClassName).append(";").append(System.lineSeparator());
    
    // 生成interface的描述
    code.append("/**").append(System.lineSeparator());
    code.append(" * ").append(simpleClassName).append("模型的数据层自定义接口，可以由程序员根据实际情况完善").append(System.lineSeparator());
    code.append(" * @author saturn").append(System.lineSeparator());
    code.append(" */").append(System.lineSeparator());
    
    // 生成interface的定义和默认的分页方法定义
    code.append("public interface ").append(customRepositorySimpleClass).append(" {").append(System.lineSeparator());
    code.append("  /**").append(System.lineSeparator());
    code.append("   * 这是分页方法的雏形，可以根据业务要求进行修改").append(System.lineSeparator());
    code.append("   */").append(System.lineSeparator());
    code.append("  Page<").append(simpleClassName).append("> queryPage(Pageable pageable , Map<String, Object> conditions);").append(System.lineSeparator());
    code.append("}").append(System.lineSeparator());
    return code;
  }
  
  private StringBuffer buildCustomRepositoryImplFileContext(PersistentClass persistentClass , String rootPackage) {
    /*
     * 生成的内容效果示意：
     * 
     * package com.vanda.platform.saturn.test.projectname.repository.internal;
     * import XXXXXX;
     * /**
     *  * 注释自动生成
     *  * @author saturn
     *  *
     * public class AlarmEventRepositoryImpl implements AlarmEventRepositoryCustom {
     *   @Autowired
     *   @PersistenceContext
     *   private EntityManager entityManager;
     *   @Override
     *   public Page<AlarmEventEntity> queryPage(Pageable pageable, Map<String, Object> conditions) {
     *     // TODO 这里的代码需要开发人员自行完善——使用JPA
     *     return null;
     *   }
     * }
     * */
    String persistentClassName = persistentClass.getClassName();
    String simpleClassName = persistentClass.getSimpleClassName();
    String repositoryInternalPackage = StringUtils.join(rootPackage,".repository.internal");
    String customRepositorySimpleClass = StringUtils.join(simpleClassName , "RepositoryCustom");
    String customRepositoryImplSimpleClass = StringUtils.join(simpleClassName , "RepositoryImpl");
    
    // 开始生成内容——package和class
    StringBuffer code = new StringBuffer();
    code.append("package ").append(repositoryInternalPackage).append(";").append(System.lineSeparator());
    code.append("import java.util.Map;").append(System.lineSeparator());
    code.append("import javax.persistence.EntityManager;").append(System.lineSeparator());
    code.append("import javax.persistence.PersistenceContext;").append(System.lineSeparator());
    code.append("import org.springframework.beans.factory.annotation.Autowired;").append(System.lineSeparator());
    code.append("import org.springframework.data.domain.Page;").append(System.lineSeparator());
    code.append("import org.springframework.data.domain.Pageable;").append(System.lineSeparator());
    code.append("import org.springframework.beans.factory.annotation.Autowired;").append(System.lineSeparator());
    code.append("import ").append(persistentClassName).append(";").append(System.lineSeparator());
    
    // 生成class的描述
    code.append("/**").append(System.lineSeparator());
    code.append(" * ").append(simpleClassName).append("模型的数据层自定义接口实现，可以由程序员根据实际情况完善").append(System.lineSeparator());
    code.append(" * @author saturn").append(System.lineSeparator());
    code.append(" */").append(System.lineSeparator());
    
    // 生成class的类定义信息和默认的分页方法实现
    code.append("public class ").append(customRepositoryImplSimpleClass).append(" implements ").append(customRepositorySimpleClass).append(" {").append(System.lineSeparator());
    code.append("  @Autowired").append(System.lineSeparator());
    code.append("  @PersistenceContext").append(System.lineSeparator());
    code.append("  private EntityManager entityManager;").append(System.lineSeparator());
    code.append("  @Override").append(System.lineSeparator());
    code.append("  public Page<").append(simpleClassName).append("> queryPage(Pageable pageable, Map<String, Object> conditions) {").append(System.lineSeparator());
    code.append("    // TODO 这里的代码需要开发人员自行完善——使用JPA").append(System.lineSeparator());
    code.append("    return null;").append(System.lineSeparator());
    code.append("  }").append(System.lineSeparator());
    code.append("}").append(System.lineSeparator());
    return code;
  }
  
  /**
   * buildRelationQueryMethods构造了普通的关联方法，
   * buildCustomUpdateMethods构造了自定义更新方法，
   * buildCustomQueryMethods构造了自定义查询方法，
   * 但是这些方法构造的都是java文件的一部分，还需要一个私有方法构造java文件内容的其它部分，这个方法就是buildJavaFileContext方法
   */
  private StringBuffer buildRepositoryFileContext(PersistentClass persistentClass , Map<String, String> importMapping , String rootPackage , PersistentClassService persistentClassService , PersistentPropertyService persistentPropertyService , PersistentRelationService persistentRelationService) {
    /*
     * 构造示例如下：
     * package XXXXXXXX.repository;
     * import XXXXXXX;
     * 
     * 注释自动生成
     * @author saturn
     * @Repository("_AlarmEventRepository")
     * public interface AlarmEventRepository extends
     * JpaRepository<AlarmEventEntity, String>,
     * JpaSpecificationExecutor<AlarmEventEntity>,
     * AlarmEventRepositoryCustom {
     * ......
     * }
     * 
     * ========
     * 
     * 这样来说，构造步骤包括：
     * 1、构造package部分和import部分
     * 2、构造类名定义和继承体系
     * 3、组装定义内容，并最终形成一个XXXXRepository.java的文件内容
     * 4、一旦文件内容创建成功构建成功
     */
    String simpleClassName = persistentClass.getSimpleClassName();
    String className = persistentClass.getClassName();
    StringBuffer javacontexts = new StringBuffer();
    String repositoryPacke = StringUtils.join(rootPackage,".repository");
    String repositorySimpleClassName = StringUtils.join(simpleClassName , "Repository");
    String repositoryInternalPacke = StringUtils.join(rootPackage,".repository.internal");
    String repositoryInternalSimpleClassName = StringUtils.join(simpleClassName , "RepositoryCustom");
    String repositoryInternalClassName = StringUtils.join(repositoryInternalPacke, "." , repositoryInternalSimpleClassName);
    PersistentProperty primaryKey = persistentPropertyService.findPrimaryKey(className);
    String primaryKeyClass = primaryKey.getPropertyClass();
    importMapping.put(primaryKeyClass , primaryKeyClass);
    importMapping.put("org.springframework.data.jpa.repository.Modifying" , "org.springframework.data.jpa.repository.Modifying");
    
    // 1、=============
    javacontexts.append("package " + repositoryPacke + ";" + System.lineSeparator());
    if(importMapping == null || importMapping.isEmpty()) {
      LOGGER.error("错误的import参数信息，请检查!!");
      return null;
    }
    importMapping.put("org.springframework.data.jpa.repository.JpaRepository", "org.springframework.data.jpa.repository.JpaRepository");
    importMapping.put("org.springframework.data.jpa.repository.JpaSpecificationExecutor" , "org.springframework.data.jpa.repository.JpaSpecificationExecutor");
    importMapping.put("org.springframework.stereotype.Repository" , "org.springframework.stereotype.Repository");
    importMapping.put(className, className);
    if(persistentClass.getBuildCustomRepository()) {
      importMapping.put(repositoryInternalClassName, repositoryInternalClassName);
    }
    
    // 2、 ============
    StringBuffer javaBodyContext = new StringBuffer();
    javaBodyContext.append(System.lineSeparator());
    javaBodyContext.append("/**").append(System.lineSeparator());
    javaBodyContext.append(" * ").append(simpleClassName).append("业务模型的数据库方法支持").append(System.lineSeparator());
    javaBodyContext.append(" * @author saturn").append(System.lineSeparator());
    javaBodyContext.append(" */").append(System.lineSeparator());
    javaBodyContext.append("@Repository(\"_").append(simpleClassName).append("Repository\")").append(System.lineSeparator());
    javaBodyContext.append("public interface ").append(repositorySimpleClassName).append(System.lineSeparator());
    javaBodyContext.append("    extends").append(System.lineSeparator());
    javaBodyContext.append("      JpaRepository<").append(TStringUtils.getSimpleClassName(className)).append(", ").append(TStringUtils.getSimpleClassName(primaryKeyClass)).append(">").append(System.lineSeparator());
    javaBodyContext.append("      ,JpaSpecificationExecutor<").append(TStringUtils.getSimpleClassName(className)).append(">").append(System.lineSeparator());
    if(persistentClass.getBuildCustomRepository()) {
      javaBodyContext.append("      ,").append(repositoryInternalSimpleClassName).append(" ").append(System.lineSeparator());
    }
    javaBodyContext.append("  {").append(System.lineSeparator());
    
    // 3、======================
    // 首先是普通的关联查询
    StringBuffer methodContexts = this.buildRelationQueryMethods(persistentClass, importMapping, persistentPropertyService);
    javaBodyContext.append(methodContexts);
    javaBodyContext.append(System.lineSeparator());
    // 然后是自定义查询
    methodContexts = this.buildCustomQueryMethods(persistentClass, persistentClassService, persistentPropertyService, persistentRelationService, importMapping);
    javaBodyContext.append(methodContexts);
    javaBodyContext.append(System.lineSeparator());
    // 最后是自定义更新
    methodContexts = this.buildCustomUpdateMethods(persistentClass, importMapping, persistentPropertyService, persistentRelationService);
    javaBodyContext.append(methodContexts);
    javaBodyContext.append(System.lineSeparator());    
    
    // 最后再排序import，这样出来的import代码信息更具有可读性
    List<String> importsList = new LinkedList<>();
        importMapping.keySet()
        .stream().sorted((target , souce) -> StringUtils.compare(target, souce))
        .forEach(importItem -> importsList.add("import " + importItem + ";"));
    String importContext = StringUtils.join(importsList.toArray(new String[]{}), System.lineSeparator());
    javacontexts.append(importContext).append(System.lineSeparator());
    javacontexts.append(javaBodyContext);
    javacontexts.append("}");
    
    LOGGER.debug("===================" + repositorySimpleClassName + ":" + javacontexts.toString());
    return javacontexts;
  }
  
  /**
   * TODO 继续写 
   * TODO 有难度，还没有测试
   * @param persistentClass
   * @param importMapping
   * @return 
   */
  private StringBuffer buildCustomQueryMethods(PersistentClass persistentClass , PersistentClassService persistentClassService , PersistentPropertyService persistentPropertyService , PersistentRelationService persistentRelationService , Map<String, String> importMapping) {
    StringBuffer code = new StringBuffer(); 
    List<PersistentQueryMethod> persistentQueryMethods = persistentClass.getQueryMethods();
    String simpleClassName = persistentClass.getSimpleClassName();
    if(persistentQueryMethods == null || persistentQueryMethods.isEmpty()) {
      return code;
    }
    
    /*
     * 构造效果如下 ======
     * 
     * 按照指定的单位，查询这个单位下拥有指定角色（名）的人员信息。
     * @param userEntity_alarmUnit_id 指定的单位信息（单位编号）
     * @param userEntity_roles_name 指定的角色名
     * @param userEntity_useStatus 用户状态
     * @Query("select distinct m from UserEntity userEntity "
     *  + " left join fetch userEntity.roles userEntity_roles "
     *  + " left join fetch userEntity.alarmUnit userEntity_alarmUnit "
     *  + " where userEntity_alarmUnit.id = :userEntity_alarmUnit_id and userEntity_roles.name = :userEntity_roles_name and userEntity.useStatus = :userEntity_useStatus"
     *  + " order by userEntity.id desc")
     *  public List<UserEntity> findByUserRoles(@Param("userEntity_alarmUnit_id") String userEntity_alarmUnit_id ,@Param("userEntity_roles_name") String userEntity_roles_name , @Param("userEntity_useStatus") Integer userEntity_useStatus);
     */
    
    // TODO 这个方法存在递归，比较复杂，需要放置到一个私有方法中处理
    for (PersistentQueryMethod queryMethodItem : persistentQueryMethods) {
      String methodName = queryMethodItem.getMethodName();
      String description = queryMethodItem.getDescription();
      String params[] = queryMethodItem.getParams();
      QueryType queryTypes[] = queryMethodItem.getQueryTypes();
      String orderByParams[] = queryMethodItem.getOrderByParams();
      OrderType orderTypes[] = queryMethodItem.getOrderTypes();
      if(StringUtils.isBlank(description) || params == null || params.length == 0
          || queryTypes == null || queryTypes.length == 0 || params.length != queryTypes.length) {
        LOGGER.warn("自定义查询[" + simpleClassName + "]，存在参数错误，忽略一个错误的自定义查询方法的构建，请检查!!");
        continue;
      }
      
      // 递归构造方法的说明信息、左连接语句定义信息、条件参数信息、查询参数信息等
      StringBuffer descParamsContexts = new StringBuffer();
      StringBuffer leftJoinContexts = new StringBuffer();
      StringBuffer conditionParamsContexts = new StringBuffer();
      StringBuffer paramsContexts = new StringBuffer();
      String currentParamAlis = TStringUtils.letterLowercase(simpleClassName);
      for (String param : params) {
        String[] paramItems = StringUtils.split(param , ".");
        this.foundRelationParams(persistentClass, persistentClass, paramItems, 0, persistentClassService, persistentPropertyService, persistentRelationService, importMapping, descParamsContexts, leftJoinContexts, conditionParamsContexts, paramsContexts, currentParamAlis);
      }
      
      // 有了以上这些关键信息后，就可以开发构建数据层方法定义了
      code.append("  /**").append(System.lineSeparator());
      code.append("   * ").append(description).append(System.lineSeparator());
      code.append(descParamsContexts);
      code.append("   */").append(System.lineSeparator());
      // 查询条件，第一句select查询点
      code.append("  @Query(\"select distinct ").append(currentParamAlis).append(" from ").append(simpleClassName).append(" ").append(currentParamAlis).append(" \"").append(System.lineSeparator());
      // 需要构建的左外连接
      code.append(leftJoinContexts);
      // 需要构建的条件
      code.append("      + \" where ").append(conditionParamsContexts).append(" \" ");
      // 需要构造的排序条件
      if(orderByParams != null && orderTypes != null && orderByParams.length == orderTypes.length) {
        StringBuffer orderContxt = new StringBuffer(System.lineSeparator() + "      + \" order by ");
        for(int index = 0 ; index < orderByParams.length ; index++) {
          if(index != 0) {
            orderContxt.append(",");
          }
          String orderByParam = orderByParams[index];
          OrderType orderType = orderTypes[index];
          orderContxt.append(TStringUtils.letterLowercase(simpleClassName)).append(".").append(orderByParam).append(" ").append(orderType == OrderType.ASC?"asc":"desc");
        }
        code.append(orderContxt).append(" \" ");
      }
      code.append(")").append(System.lineSeparator());
      
      //构造方法定义
      importMapping.put("java.util.List", "java.util.List");
      // TODO 对于返回值的判定还可以优化
      code.append("  public List<" + simpleClassName + "> " + methodName + "(" + paramsContexts + ");");
      code.append(System.lineSeparator()).append(System.lineSeparator());
    }
    return code;
  }
  
  /**
   * TODO 非常重要的方法，但是没有写注释也还没有测试
   * TODO 这个方法和com.vanda.platform.saturn.core.scan.JavassistAnalysis.foundRelationParms类似，需要在后期版本中进行代码重复度调整
   * @param prsistentClass
   * @param paramArrayItems
   * @param itemIndex
   * @param persistentClassMapping
   * @return
   */
  private void foundRelationParams(PersistentClass rootPrsistentClass , PersistentClass currentPrsistentClass , String paramArrayItems[] , int itemIndex , PersistentClassService persistentClassService , PersistentPropertyService persistentPropertyService , PersistentRelationService persistentRelationService , Map<String, String> importMapping , StringBuffer descParamsContexts , StringBuffer leftJoinContexts , StringBuffer conditionParamsContexts ,  StringBuffer paramsContexts , String currentParamAlis) {
    Validate.notNull(rootPrsistentClass , "没有找到模型结构描述[rootPrsistentClass]!!");
    Validate.notNull(currentPrsistentClass , "没有找到模型结构描述[currentPrsistentClass]!!");
    Validate.isTrue(paramArrayItems != null && paramArrayItems.length > 0, "没有找到模型结构中的指定属性!!");
    
    /*
     * 再分析自定义查询参数使，由于参数支持级联，所以要进行区分：
     * 1、不存在级联的参数只需要构建其查询条件部分即可
     * 2、存在级联的参数，除了构建查询条件，更重要的是级联构建其左外连接串和左外连接的别名信息
     * */
    // 1、======================
    if(paramArrayItems.length == 1) {
      String simpleClassName = rootPrsistentClass.getSimpleClassName();
      String className = rootPrsistentClass.getClassName();
      String param = paramArrayItems[0];
      PersistentProperty persistentProperty = persistentPropertyService.findByPropertyName(className, param);
      if(persistentProperty == null) {
        LOGGER.warn("没有发现属性信息，该自定义查询方法的构造终止!!");
        return;
      }
      String propertyClass = persistentProperty.getPropertyClass();
      String propertyDesc = persistentProperty.getPropertyDesc();
      String buildParam = TStringUtils.letterLowercase(simpleClassName) + "_" + param;
      // 构造说明信息
      descParamsContexts.append("   * @param " + buildParam + " " + propertyDesc + System.lineSeparator());
      // 构造查询语句中的查询条件
      if(conditionParamsContexts.length() != 0) {
        conditionParamsContexts.append(" and ");
      }
      conditionParamsContexts.append(TStringUtils.letterLowercase(simpleClassName) + "." + param + "=:" + buildParam);
      // 构造方法定义中的传参部分
      if(paramsContexts.length() != 0) {
        paramsContexts.append(" , ");
      }
      importMapping.put(propertyClass, propertyClass);
      paramsContexts.append("@Param(\"" + buildParam + "\") " + TStringUtils.getSimpleClassName(propertyClass) + " " + buildParam);
    } 
    // 2、 ===================
    else if(paramArrayItems.length != 1 && paramArrayItems.length == itemIndex + 1) {
      String className = currentPrsistentClass.getClassName();
      String param = paramArrayItems[itemIndex];
      String nextParamAlis = currentParamAlis + "_" + param;
      PersistentProperty persistentProperty = persistentPropertyService.findByPropertyName(className, param);
      if(persistentProperty == null) {
        LOGGER.warn("没有发现属性信息，该自定义查询方法的构造终止!!");
        return;
      }
      String propertyClass = persistentProperty.getPropertyClass();
      String propertyDesc = persistentProperty.getPropertyDesc();
//      // 构造left join left信息
//      leftJoinContexts.append(" + \" left join fetch " + currentParamAlis + "." + param + " " + nextParamAlis + " \" " + System.lineSeparator());
      // 构造说明信息
      descParamsContexts.append("   * @param " + nextParamAlis + " " + propertyDesc + System.lineSeparator());
      // 构造查询语句中的查询条件
      if(conditionParamsContexts.length() != 0) {
        conditionParamsContexts.append(" and ");
      }
      conditionParamsContexts.append(currentParamAlis + "." + param + "=:" + nextParamAlis);
      // 构造方法定义中的传参部分
      if(paramsContexts.length() != 0) {
        paramsContexts.append(" , ");
      }
      importMapping.put(propertyClass, propertyClass);
      paramsContexts.append("@Param(\"" + nextParamAlis + "\") " + TStringUtils.getSimpleClassName(propertyClass) + " " + nextParamAlis);
    }
    // 3、============其它情况下说明关联关系还没有遍历完，需要继续遍历并记录left join fetch的关系
    else {
      String propertyName = paramArrayItems[itemIndex];
      String className = currentPrsistentClass.getClassName();
      PersistentRelation persistentRelation = persistentRelationService.findByPropertyName(className, propertyName);
      if(persistentRelation == null) {
        LOGGER.warn("没有发现关联属性信息，该自定义查询方法的构造终止!!");
        return;
      }
      
      // 取得下一个关联的class，准备开始下一次递归
      String propertyClass = persistentRelation.getPropertyClass();
      PersistentClass nextPersistentClass = persistentClassService.queryByClassName(propertyClass);
      if(nextPersistentClass == null) {
        LOGGER.warn("没有发现关联属性信息的类型，该自定义查询方法的构造终止!!");
        return;
      }
      String nextParamAlis = currentParamAlis + "_" +  propertyName;
      // 构造left join left信息
      leftJoinContexts.append("      + \" left join fetch " + currentParamAlis + "." + propertyName + " " + nextParamAlis + " \" " + System.lineSeparator());
      this.foundRelationParams(rootPrsistentClass, nextPersistentClass, paramArrayItems, itemIndex+1, persistentClassService, persistentPropertyService, persistentRelationService, importMapping, descParamsContexts, leftJoinContexts, conditionParamsContexts, paramsContexts, nextParamAlis);
    }
  }
  
  /**
   * TODO 非常重要的方法，但是没有写注释也还没有测试
   * @param persistentClass
   * @param importMapping
   * @return
   */
  private StringBuffer buildCustomUpdateMethods(PersistentClass persistentClass , Map<String, String> importMapping , PersistentPropertyService persistentPropertyService , PersistentRelationService persistentRelationService) {
    String repositoryTable = persistentClass.getRepositoryTable();
    String targetClassName = persistentClass.getClassName();
    StringBuffer codeView = new StringBuffer();
    List<PersistentUpdateMethod> persistentUpdateMethods = persistentClass.getUpdateMethods();
    if(persistentUpdateMethods == null || persistentUpdateMethods.isEmpty()) {
      return codeView;
    }
    
    /* 
     * 方法的构造模板类似：=====
     * 
     * 修改用户已经设置的单位信息
     * @param userId 用户id
     * @param unitId 单位id
     * @Modifying
     * @Query(value="update b_user set alarm_unit = :unitId where id = :userId" , nativeQuery=true)
     * public void modifyUnit(@Param("userId") String userId ,@Param("unitId") String unitId);
     * */
    UPDATEITEM:for (PersistentUpdateMethod updateMethodItem : persistentUpdateMethods) {
      String description = updateMethodItem.getDescription();
      String queryParams[] = updateMethodItem.getQueryParams();
      String updateParams[] = updateMethodItem.getUpdateParams();
      String methodName = updateMethodItem.getMethodName();
      // TODO 还需要验证queryParams属性、updateParams属性的正确性
      
      // ==== 形成参数说明信息、更新语句片段信息、更新条件片段信息
      StringBuffer descParamContexts = new StringBuffer();
      StringBuffer updateContexts = new StringBuffer();
      StringBuffer conditionContexts = new StringBuffer();
      StringBuffer paramContexts = new StringBuffer();
      // 首先基于更新参数，构建说明信息、更新语句片段信息、更新条件片段信息
      for(int index = 0 ; index < updateParams.length ; index++) {
        String updateParam = updateParams[index];
        if(index != 0) {
          updateContexts.append(",");
          paramContexts.append(",");
        }
        // 如果是关联信息，则要使用关联模型上的主键信息
        PersistentProperty persistentProperty = persistentPropertyService.findByPropertyName(targetClassName, updateParam);
        if(persistentProperty != null) {
          if(!persistentProperty.getCanUpdate()) {
            LOGGER.warn("关联属性[" + updateParam + "]不允许进行更新操作，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          String propertyDbName = persistentProperty.getPropertyDbName();
          String propertyClass = persistentProperty.getPropertyClass();
          String propertyDesc = persistentProperty.getPropertyDesc();
          descParamContexts.append("   * @param _" + updateParam + " " + propertyDesc + System.lineSeparator());
          updateContexts.append(propertyDbName + "=:_" + updateParam);
          importMapping.put(propertyClass, propertyClass);
          importMapping.put("org.springframework.data.repository.query.Param", "org.springframework.data.repository.query.Param");
          paramContexts.append("@Param(\"_" + updateParam + "\")" + TStringUtils.getSimpleClassName(propertyClass) + " _" + updateParam);
        } 
        // 否则就是从关联字段中取出
        else {
          PersistentRelation persistentRelation = persistentRelationService.findByPropertyName(targetClassName, updateParam);
          if(persistentRelation == null) {
            LOGGER.warn("未找到属性[" + updateParam + "]，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          if(!persistentRelation.getCanUpdate()) {
            LOGGER.warn("关联属性[" + updateParam + "]不允许进行更新操作，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          RelationType relationType = persistentRelation.getRelationType();
          if(relationType == RelationType.ManyToMany || relationType == RelationType.ManyToOne) {
            LOGGER.warn("属性[" + updateParam + "]关联方式存在异常，请检查，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          String propertyDbName = persistentRelation.getPropertyDbName();
          String propertyDesc = persistentRelation.getPropertyDesc();
          String propertyClass = persistentRelation.getPropertyClass();
          // 找到关联类的主键信息
          PersistentProperty relationPrimaryKey = persistentPropertyService.findPrimaryKey(propertyClass);
          if(relationPrimaryKey == null) {
            LOGGER.warn("关联属性[" + updateParam + "]没有设定主键信息，请检查，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          propertyClass = relationPrimaryKey.getPropertyClass();
          
          descParamContexts.append("   * @param _" + updateParam + " " + propertyDesc + System.lineSeparator());
          updateContexts.append(propertyDbName + "=:_" + updateParam);
          importMapping.put(propertyClass, propertyClass);
          importMapping.put("org.springframework.data.repository.query.Param", "org.springframework.data.repository.query.Param");
          paramContexts.append("@Param(\"_" + updateParam + "\")" + TStringUtils.getSimpleClassName(propertyClass) + " _" + updateParam);
        }
      }
      // 然后基于查询信息，构建说明信息、更新语句片段信息、更新条件片段信息
      for (int index = 0 ; index < queryParams.length ; index++) {
        String queryParamItem = queryParams[index];
        if(index != 0) {
          conditionContexts.append(" AND ");
        }
        paramContexts.append(",");
        PersistentProperty persistentProperty = persistentPropertyService.findByPropertyName(targetClassName, queryParamItem);
        if(persistentProperty != null) {
          String propertyDbName = persistentProperty.getPropertyDbName();
          String propertyDesc = persistentProperty.getPropertyDesc();
          String propertyClass = persistentProperty.getPropertyClass();
          descParamContexts.append("   * @param " + queryParamItem + " " + propertyDesc + System.lineSeparator());
          conditionContexts.append(propertyDbName + "=:" + queryParamItem);
          importMapping.put(propertyClass, propertyClass);
          paramContexts.append("@Param(\"" + queryParamItem + "\")" + TStringUtils.getSimpleClassName(propertyClass) + " " + queryParamItem);
        } else {
          // 如果条件成立说明在该类模型的普通属性和关联属性中都没有找到这个属性，或者说明这个关联属性的关联类型错误，不能进行属性更新
          PersistentRelation persistentRelation = persistentRelationService.findByPropertyName(targetClassName, queryParamItem);
          if(persistentRelation == null) {
            LOGGER.warn("未找到属性[" + queryParamItem + "]，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          RelationType relationType = persistentRelation.getRelationType();
          if(relationType == RelationType.ManyToMany || relationType == RelationType.ManyToOne) {
            LOGGER.warn("属性[" + queryParamItem + "]关联方式存在异常，请检查，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          String propertyDbName = persistentRelation.getPropertyDbName();
          String propertyDesc = persistentRelation.getPropertyDesc();
          String propertyClass = persistentRelation.getPropertyClass();
          // 找到关联类的主键信息
          PersistentProperty relationPrimaryKey = persistentPropertyService.findPrimaryKey(propertyClass);
          if(relationPrimaryKey == null) {
            LOGGER.warn("关联属性[" + queryParamItem + "]没有设定主键信息，请检查，忽略该自定义更新操作的创建过程!!");
            continue UPDATEITEM;
          }
          propertyClass = relationPrimaryKey.getPropertyClass();
          descParamContexts.append("   * @param " + queryParamItem + " " + propertyDesc + System.lineSeparator());
          conditionContexts.append(propertyDbName + "=:" + queryParamItem);
          importMapping.put(propertyClass, propertyClass);
          paramContexts.append("@Param(\"" + queryParamItem + "\")" + TStringUtils.getSimpleClassName(propertyClass) + " " + queryParamItem + " ");
        }
      }
      
      // ======= 准备工作做完了，开始构建代码块
      codeView.append("  /**" + System.lineSeparator());
      codeView.append("   * " + description + System.lineSeparator());
      codeView.append(descParamContexts);
      codeView.append("   */" + System.lineSeparator());
      // 然后是更新语句的定义
      codeView.append("  @Modifying" + System.lineSeparator());
      // 然后是查询语句
      codeView.append("  @Query(value=\"update " + repositoryTable + " set " + updateContexts + " where " + conditionContexts + "\" , nativeQuery=true)" + System.lineSeparator());
      // 然后是方法定义
      codeView.append("  public void " + methodName + "(" + paramContexts + ");" + System.lineSeparator());
      codeView.append(System.lineSeparator());
    }
    
    return codeView;
  }
  
  /**
   * TODO 非常重要的方法，但是没有写注释也还没有测试
   * @param persistentClass
   * @param importMapping
   * @param persistentPropertyService
   */
  private StringBuffer buildRelationQueryMethods(PersistentClass persistentClass , Map<String, String> importMapping , PersistentPropertyService persistentPropertyService) {
    String targetSimpleClassName = persistentClass.getSimpleClassName();
    StringBuffer codeView = new StringBuffer();
    List<PersistentRelation> persistentRelations = persistentClass.getRelations();
    if(persistentRelations == null || persistentRelations.isEmpty()) {
      return codeView;
    }
    
    for (PersistentRelation persistentRelation : persistentRelations) {
      String persistentClassName = persistentRelation.getPersistentClassName();
      // 找到模型的主键定义信息，因为等一下方法参数的类型会用到
      // TODO 这里是否需要一个私有方法，等一下看后续的代码再说
      Map<String, PersistentProperty> persistentPropertyMapping = persistentPropertyService.findByClassName(persistentClassName);
      if(persistentPropertyMapping == null || persistentPropertyMapping.isEmpty()) {
        continue;
      }
      Collection<PersistentProperty> persistentProperties = persistentPropertyMapping.values();
      List<PersistentProperty> primaryProperties = persistentProperties.stream().filter(persistentProperty -> persistentProperty.getPrimaryKey()).collect(Collectors.toList());
      if(primaryProperties == null ||  primaryProperties.size() != 1) {
        continue;
      }
      PersistentProperty primaryProperty = primaryProperties.get(0);
      // TODO 基础验证要做
      String property = persistentRelation.getPropertyName();
      String propertyDesc = persistentRelation.getPropertyDesc();
      String paramName = property + "Id";
      
      /*
       * 方法的构造模板类似：=====
       * 
       * 按照单位信息的数据层编号，查询这个单位下的所有用户，并按照listsort正序排列（且无论这些用户的状态如何）
       * @param unitId 警署编号信息
       * @Query("from UserEntity u left join fetch u.alarmUnit au where au.id = :unitId order by u.listsort desc")
       * public List<UserEntity> findByUnit(@Param("unitId") String unitId);
       * */
      codeView.append("  /**" + System.lineSeparator());
      codeView.append("   * 按照" + propertyDesc + "进行查询" + System.lineSeparator());
      codeView.append("   * @param " + paramName + " " + propertyDesc + System.lineSeparator());
      codeView.append("   */" + System.lineSeparator());
      importMapping.put("org.springframework.data.jpa.repository.Query", "org.springframework.data.jpa.repository.Query");
      importMapping.put("org.springframework.data.repository.query.Param", "org.springframework.data.repository.query.Param");
      importMapping.put(primaryProperty.getPropertyClass(), primaryProperty.getPropertyClass());
      importMapping.put("java.util.List", "java.util.List");
      // 查询设定
      codeView.append("  @Query(\"from " + targetSimpleClassName + " " + TStringUtils.letterLowercase(targetSimpleClassName) + " \" " + System.lineSeparator()
          + "  + \" left join fetch " + TStringUtils.letterLowercase(targetSimpleClassName) + "." + property + " " + property + " \" " + System.lineSeparator()
          + "  + \" where " + property + "." + primaryProperty.getPropertyName() + " = :" + paramName + "\")"  + System.lineSeparator());
      // 查询方法名设定
      codeView.append("  public List<" + targetSimpleClassName + "> findBy" + TStringUtils.letterUppercase(property) + 
        "(@Param(\"" + paramName + "\") " + TStringUtils.getSimpleClassName(primaryProperty.getPropertyClass()) + " " + paramName + ");" + System.lineSeparator());
      codeView.append("  " + System.lineSeparator());
      
      // TODO 方法名是否需要映射，以免后续重复
    }
    
    return codeView;
  }
}