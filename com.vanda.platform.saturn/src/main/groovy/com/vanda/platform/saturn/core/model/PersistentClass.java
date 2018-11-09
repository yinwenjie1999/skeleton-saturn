package com.vanda.platform.saturn.core.model;

import java.util.List;

/**
 * 持久化类描述
 * @author yinwenjie
 */
public class PersistentClass {
  /**
   * 所属域
   * 域描述信息（其特点是一个域下的有多个对象，且这些对象拥有非常强的内聚性）
   */
  private String domain;
  /**
   * 所属完整包名
   */
  private String pkage;
  /**
   * 完整类名
   */
  private String className;
  /**
   * 不包括报名的类型。
   * 注意，骨架的这个版本不支持内部类的定义
   */
  private String simpleClassName;
  /**
   * 该标记表示这个对象定义是否是基于JPA的持久层对象<br>
   * 如果是持久层对象才会生成相关的持久层代码，否则就不会生成
   */
  private Boolean repositoryEntity = false;
  /**
   * 如果是一个JPA持久层对象模型，就需要记录持久层的数据表名字
   */
  private String repositoryTable;
  /**
   * 该持久化描述类是否需要激活自定义持久层类（基于JPA）
   */
  private Boolean buildCustomRepository = false;
  /**
   * 这个持久化类中的所有属性（有排序）信息。
   */
  private List<PersistentProperty> properties;
  /**
   * 这个持久化类中的关联信息（有排序）
   */
  private List<PersistentRelation> relations;
  /**
   * 由程序员在持久层对象中自定义的查询方法
   */
  private List<PersistentQueryMethod> queryMethods;
  /**
   * 由程序员在持久层对象中自定义的和业务相关的属性更新方法
   */
  private List<PersistentUpdateMethod> updateMethods;
  
  public Boolean getRepositoryEntity() {
    return repositoryEntity;
  }
  public void setRepositoryEntity(Boolean repositoryEntity) {
    this.repositoryEntity = repositoryEntity;
  }
  public String getPkage() {
    return pkage;
  }
  public void setPkage(String pkage) {
    this.pkage = pkage;
  }
  public String getClassName() {
    return className;
  }
  public void setClassName(String className) {
    this.className = className;
  }
  public List<PersistentProperty> getProperties() {
    return properties;
  }
  public void setProperties(List<PersistentProperty> properties) {
    this.properties = properties;
  }
  public List<PersistentRelation> getRelations() {
    return relations;
  }
  public void setRelations(List<PersistentRelation> relations) {
    this.relations = relations;
  }

  public Boolean getBuildCustomRepository() {
    return buildCustomRepository;
  }

  public void setBuildCustomRepository(Boolean buildCustomRepository) {
    this.buildCustomRepository = buildCustomRepository;
  }

  public List<PersistentQueryMethod> getQueryMethods() {
    return queryMethods;
  }

  public void setQueryMethods(List<PersistentQueryMethod> queryMethods) {
    this.queryMethods = queryMethods;
  }
  
  public List<PersistentUpdateMethod> getUpdateMethods() {
    return updateMethods;
  }
  public void setUpdateMethods(List<PersistentUpdateMethod> updateMethods) {
    this.updateMethods = updateMethods;
  }
  public String getSimpleClassName() {
    return simpleClassName;
  }
  public void setSimpleClassName(String simpleClassName) {
    this.simpleClassName = simpleClassName;
  }
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
  public String getRepositoryTable() {
    return repositoryTable;
  }
  public void setRepositoryTable(String repositoryTable) {
    this.repositoryTable = repositoryTable;
  }
}
