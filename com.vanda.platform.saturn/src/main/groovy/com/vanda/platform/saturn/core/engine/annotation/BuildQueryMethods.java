package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该注解加载在指定的实体类定义上，表明需要saturn V3.0按照描述生成指定属性下的查询方法<br>
 * 该注解需要和QueryMethod注解配合使用
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BuildQueryMethods {
  /**
   * 指定的方法描述信息
   */
  QueryMethod[] method();
}