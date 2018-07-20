package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该注解加载在指定的实体类定义上，表明需要saturn V3.0完成自定义存储层的类模板生成<br>
 * 在这个生成的类中，将有一个初始定义的分页方法
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface BuildCustomRepository {

}