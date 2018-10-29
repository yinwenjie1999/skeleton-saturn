package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 用于在相关实体定义类上，表示这是一个需要骨架组件进行扫描的实体定义
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface SaturnEntity {
  /**
   * 可能设定的实体所属域信息，在代码生成阶段，不同的域接口以及实现类会存放在不同的包中
   * @return
   */
  String domain() default "";
  
  /**
   * 实体定义类的名字，一般来说就是类名（只能是英文）
   */
  String name();
  
  /**
   * 实体定义类的描述，这个描述将被各层的代码生成引擎使用
   */
  String description();
}