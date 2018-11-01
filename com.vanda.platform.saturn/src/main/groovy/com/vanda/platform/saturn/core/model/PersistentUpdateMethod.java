package com.vanda.platform.saturn.core.model;

/**
 * 关于一个持久层类，自定义的和业务有关的更新方法，在这里被描述
 * @author yinwenjie
 */
public class PersistentUpdateMethod {
  /**
   * 该模型属性所属的class信息——完整的class信息
   */
  private String persistentClassName;
  /**
   * 指定的属性名（条件），这些属性名一定要存在于当前的类定义中，否则在saturn进行扫描的时候，就会报错<br>
   * 查询属性很关键，它（们）指明了需要更新的数据范围
   */
  private String[] queryParams;
  /**
   * 指定的更新属性名（写操作），这些属性名一定要存在于当前的类定义中，否则在saturn进行扫描的时候，就会报错<br>
   * 更新属性很关键，它（们）指明了需要更新的最终值
   */
  private String[] updateParams;
  /**
   * 更新方法的描述信息
   */
  private String description;
  
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String[] getQueryParams() {
    return queryParams;
  }
  public void setQueryParams(String[] queryParams) {
    this.queryParams = queryParams;
  }
  public String[] getUpdateParams() {
    return updateParams;
  }
  public void setUpdateParams(String[] updateParams) {
    this.updateParams = updateParams;
  }
  public String getPersistentClassName() {
    return persistentClassName;
  }
  public void setPersistentClassName(String persistentClassName) {
    this.persistentClassName = persistentClassName;
  }
}