package com.vanda.platform.saturn.core.context.service;

import java.util.List;

import com.vanda.platform.saturn.core.model.PersistentQueryMethod;

/**
 * 和QueryMethod对象相关的上下文服务封装操作在这里定义（主要是查询类型的操作）。
 * @author yinwenjie
 */
public interface PersistentQueryMethodService {
  /**
   * 保存一个自定义查询描述，如果存储层已经有了这个自定义查询信息，则进行更新；如果没有则新建保存
   * @param property
   */
  public void save(PersistentQueryMethod queryMethod);
  /**
   * 按照指定的完整类名，查询这个类定义中被设定的所有“自定义”查询方法
   * @param className 指定的完整类名
   * @return
   */
  public List<PersistentQueryMethod> queryByClassName(String className);
}