package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该标注放置在实体定义类中的某一个属性上，表示这个属性是需要骨架V3.0版本进行扫描的<br>
 * 注意要使该注解生效，其类上必须使用SaturnEntity进行标注
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target({FIELD})
public @interface SaturnColumn {
  /**
   * 字段中文意义
   */
  String description();
  /**
   * 该字段是否是数据表的主键信息<br>
   * 目前骨架版本不支持符合主键（如果扫描到符合主键就会直接报错）
   */
  boolean pkColumn() default false;
  /**
   * 该字段是否可以为null
   */
  boolean nullable() default true;
  /**
   * 该字段是否在存储层是一个唯一的存在
   */
  boolean unique() default false;
  /**
   * 该字段是否允许在基础方法中（insert/save方法中）进行插入操作
   */
  boolean insertable() default false;
  /**
   * 该字段是否允许在基础方法中（update/save方法中）进行更新操作
   */
  boolean updatable() default false;
}