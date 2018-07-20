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
   * 域名称
   */
  private String name;
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
  private List<QueryMethod> queryMethods;
  /**
   * 有程序员在持久层对象中自定义的和业务相关的属性更新方法
   */
  private List<IndependentUpdateMethod> independentUpdateMethods;
  
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

  public List<QueryMethod> getQueryMethods() {
    return queryMethods;
  }

  public void setQueryMethods(List<QueryMethod> queryMethods) {
    this.queryMethods = queryMethods;
  }

  public List<IndependentUpdateMethod> getIndependentUpdateMethods() {
    return independentUpdateMethods;
  }

  public void setIndependentUpdateMethods(List<IndependentUpdateMethod> independentUpdateMethods) {
    this.independentUpdateMethods = independentUpdateMethods;
  }
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getDomain() {
    return domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
}
