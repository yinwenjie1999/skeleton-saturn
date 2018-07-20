package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 在BuildQueryMethods注解中的每一个QueryMethod注解，代表一个指定的查询方法<br>
 * 例如：
 * //@buildQueryMethods({<br>
 *   method=(params={"createTime"} , queryType=between),<br>
 *   method=(params={"alarmPhone"} , queryType=equal),<br>
 *   method=(params={"alarmPeople","alarmStatus"} , queryType=equal)<br>
 *   })<br>
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface QueryMethod {
  /**
   * 需要进行查询的属性信息，这些信息必须存在于当前类定义上
   */
  String[] params();
  
  /**
   * 查询类型，每一个查询属性都需要指定它的查询类型
   */
  QueryType[] queryType();
  
  /**
   * 目前查询类型包括：“等于”，“范围”，小于(用于数字)，小于等于（用于数字）、大于（用于数字）、大于等于（用于数字）
   * @author yinwenjie
   */
  public enum QueryType {
    equal, between, lessThan, lessEqualThan , greaterThan , greaterEqualThan
  }
}
