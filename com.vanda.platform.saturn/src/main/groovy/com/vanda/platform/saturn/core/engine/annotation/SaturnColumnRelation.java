package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * TODO 没有注释
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface SaturnColumnRelation {
  /**
   * 确定的一种对象间关联关系
   */
  RelationType type();
  
  enum RelationType {
    MANYTOONE,MANYTOMANY,ONETOMANY,ONETOONE
  }
}