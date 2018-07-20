package com.vanda.platform.saturn.core.context.repository;

import java.util.List;

import com.vanda.platform.saturn.core.model.QueryMethod;

/**
 * 和QueryMethod对象相关的数据层操作在这里进行（主要是查询类型的操作）。
 * @author yinwenjie
 */
public interface QueryMethodRepository {
  /**
   * 按照指定的完整类名，查询这个类定义中被设定的所有“自定义”查询方法
   * @param className 指定的完整类名
   * @return
   */
  public List<QueryMethod> queryByClassName(String className);
}