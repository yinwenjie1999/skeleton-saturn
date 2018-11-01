package com.vanda.platform.saturn.core.context.service;

import java.util.List;

import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

/**
 * 和updateMethod对象相关的上下文服务操作在这里进行。
 * @author yinwenjie
 */
public interface PersistentUpdateMethodService {
  /**
   * 保存一个自定义更新操作描述，如果存储层已经有了这个自定义更新信息，则进行更新；如果没有则新建保存
   * @param property
   */
  public void save(PersistentUpdateMethod queryMethod);
  /**
   * 按照完整的类全名，查询这个持久化类下定义所有自定义更新方法，以及这些方法信息的关联信息
   * @param className 完整的类全名信息
   * @return
   */
  public List<PersistentUpdateMethod> queryByClassName(String className);
}