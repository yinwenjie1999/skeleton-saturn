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
 * TODO 目前版本不支持left关联，在后续小版本中进行修正
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SaturnQueryMethod {
  /**
   * 需要进行查询的属性信息，这些信息必须存在于当前类定义上
   */
  String[] params();
  
  /**
   * 查询类型，每一个查询属性都需要指定它的查询类型
   */
  QueryType[] queryType();
  
  /**
   * 该方法的业务性质注释说明
   */
  String description();
  
  /**
   * 如果查询结果需要排序，则可以在这里进行设置，
   * 注意这里设置的属性名，必须是简单类型。
   * @return
   */
  String[] orderByParams() default "";
  
  /**
   * 如果指定的排序的依赖属性，就必须指定排序的方式，包括两种ASC和DESC
   * @return
   */
  OrderType[] orderType() default OrderType.ASC;
  
  /**
   * 目前排序类型包括：“等于”，“范围”，小于(用于数字)，小于等于（用于数字）、大于（用于数字）、大于等于（用于数字）
   * @author yinwenjie
   */
  public enum QueryType {
    EQUAL, BETWEEN, LESSTHAN, LESSEQUALTHAN , GREATERTHAN , GREATEREQUALTHAN
  }
  
  /**
   * 目前排序类型包括：正向和逆向
   * @author yinwenjie
   */
  public enum OrderType {
    DESC,ASC
  }
}