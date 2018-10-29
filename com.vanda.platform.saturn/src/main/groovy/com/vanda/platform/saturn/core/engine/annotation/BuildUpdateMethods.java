package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该注解加载在指定的实体类定义上，表明需要saturn V3.0按照要求生成指定属性的update方法。<br>
 * 该注解主要和IndependentUpdateMethod注解形成对照。
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BuildUpdateMethods {
  /**
   * 指定的方法信息
   */
  SaturnUpdateMethod[] methods();
}
