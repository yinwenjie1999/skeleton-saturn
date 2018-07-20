package com.vanda.platform.saturn.core.model;

/**
 * 关于一个持久层类，自定义的和业务有关的更新方法，在这里被描述
 * @author yinwenjie
 */
public class IndependentUpdateMethod {
  /**
   * 该查询方法的目标属性名
   */
  private String[] params;
  
  /**
   * 更新方法的描述信息
   */
  private String description;

  public String[] getParams() {
    return params;
  }

  public void setParams(String[] params) {
    this.params = params;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}