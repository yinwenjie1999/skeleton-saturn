package com.vanda.platform.saturn.core.engine.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * 该标注可放在字段上，表示该实体中的这个属性是否需要关注业务性规则判断。
 * 目前包括的业务性规则验证有：身份证（18位）、移动电话、邮箱地址、只能是数字、只能是字母（包括大小写）、只能是中文
 * @author yinwenjie
 */
@Retention(RUNTIME)
@Target(value={ElementType.FIELD})
public @interface Validate {
  
  /**
   * 指定的业务规则判断类型，每一个字段属性只能设定一种业务规则性判断
   */
  ValidateType type();
  
  enum ValidateType {
    idcard,mobilePhone,email,onlyNumber,onlyLetters,onlyChinese 
  }
}