package com.vanda.platform.saturn.core.model;

/**
 * 在持久层定义的查询方法，在这里进行记录
 * @author yinwenjie
 */
public class PersistentQueryMethod {
  // 查询类型目前包括：等于、范围、小于、小于等于、大于、大于等于
  public enum QueryType {
    EQUAL, BETWEEN, LESSTHAN, LESSEQUALTHAN, GREATERTHAN, GREATEREQUALTHAN
  }
  /**
   * 目前排序类型包括：正向和逆向
   */
  public enum OrderType {
    DESC,ASC
  }
  /**
   * 该查询方法的目标属性名
   */
  private String[] params;
  /**
   * 方法的描述信息，在进行类分析时会验证必填
   */
  private String description;
  /**
   * 查询类型默认为“等于”
   */
  private QueryType[] queryTypes;
  /**
   * 如果查询结果需要排序，则可以在这里进行设置，
   * 注意这里设置的属性名，必须是简单类型。
   */
  private String[] orderByParams;
  
  /**
   * 如果指定的排序的依赖属性，就必须指定排序的方式，包括两种ASC和DESC
   */
  private OrderType[] orderTypes;
  
  public String[] getParams() {
    return params;
  }
  public void setParams(String[] params) {
    this.params = params;
  }
  public QueryType[] getQueryTypes() {
    return queryTypes;
  }
  public void setQueryTypes(QueryType[] queryTypes) {
    this.queryTypes = queryTypes;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public String[] getOrderByParams() {
    return orderByParams;
  }
  public void setOrderByParams(String[] orderByParams) {
    this.orderByParams = orderByParams;
  }
  public OrderType[] getOrderTypes() {
    return orderTypes;
  }
  public void setOrderTypes(OrderType[] orderTypes) {
    this.orderTypes = orderTypes;
  }  
}