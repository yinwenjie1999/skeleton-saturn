package com.vanda.platform.saturn.core.context.repository;

import java.util.List;

import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

/**
 * 和IndependentUpdateMethod对象相关的数据层操作在这里进行。
 * @author yinwenjie
 */
public interface IndependentUpdateMethodRepository {
  /**
   * 按照完整的类全名，查询这个持久化类下定义所有自定义更新方法，以及这些方法信息的关联信息
   * @param className 完整的类全名信息
   * @return
   */
  public List<PersistentUpdateMethod> queryByClassName(String className);
}