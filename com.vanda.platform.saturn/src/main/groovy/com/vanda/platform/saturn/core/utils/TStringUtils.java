package com.vanda.platform.saturn.core.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 专门用来处理代码构造过程中经常进行的字符串操作
 * @author yinwenjie
 */
public final class TStringUtils {
  
  private TStringUtils() {
    
  }
  
  /**
   * 将一个字符串（一般是一个单词）的首字母小写后进行返回
   * @param context
   * @return
   */
  public static String letterLowercase(String context) {
    if(StringUtils.isBlank(context)) {
      return "";
    }
    
    return (new StringBuilder()).append(Character.toLowerCase(context.charAt(0))).append(context.substring(1)).toString();
  }
  
  /**
   * 将一个字符串（一般是一个单词）的首字母大写后进行返回
   * @param context
   * @return
   */
  public static String letterUppercase(String context) {
    if(StringUtils.isBlank(context)) {
      return "";
    }
    
    return (new StringBuilder()).append(Character.toUpperCase(context.charAt(0))).append(context.substring(1)).toString();
  }
  
  /**
   * 这个工具方法从一个完整的classname中提取类名，
   * 实际上就是在类全称中去掉包信息
   * @return 如果不是一个class的全称，则返回一个空字符串
   */
  public static String getSimpleClassName(String className) {
    if(StringUtils.isBlank(className) || className.indexOf(".") == -1) {
      return "";
    }
    
    int lastNodeIndex = StringUtils.lastIndexOf(className, ".");
    return StringUtils.substring(className, lastNodeIndex + 1);
  }
}