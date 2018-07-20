package com.vanda.platform.saturn.core.model;

/**
 * 在持久层定义的查询方法，在这里进行记录
 * @author yinwenjie
 */
public class QueryMethod {
  // 查询类型目前包括：等于、范围、小于、小于等于、大于、大于等于
  public enum QueryType {
    equal, between, lessThan, lessEqualThan, greaterThan, greaterEqualThan
  }
  
  /**
   * 该查询方法的目标属性名
   */
  private String[] params;
  /**
   * 查询类型默认为“等于”
   */
  private QueryType[] queryTypes;
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
}