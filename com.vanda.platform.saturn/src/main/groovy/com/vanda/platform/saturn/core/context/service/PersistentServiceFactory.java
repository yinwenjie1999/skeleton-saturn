package com.vanda.platform.saturn.core.context.service;

/**
 * TODO 未写注释
 * @author yinwenjie
 */
public abstract class PersistentServiceFactory {
  /**
   * 创建为“模型类”描述服务的服务层实现
   * @return
   */
  public abstract PersistentClassService createPersistentClassService();

  /**
   * 创建为“模型类中一般属性”描述服务的服务层实现
   * @return
   */
  public abstract PersistentPropertyService createPersistentPropertyService();
  
  /**
   * 创建为“模型类中一般属性”描述服务的服务层实现
   * @return
   */
  public abstract PersistentRelationService createPersistentRelationService();
  
  /**
   * 创建为“模型类中自定义查询方法的”描述服务的服务层实现
   * @return
   */
  public abstract PersistentQueryMethodService createPersistentQueryMethodService();
  
  /**
   * 创建为“模型类中自定义更新方法的”描述服务的服务层实现
   * @return
   */
  public abstract PersistentUpdateMethodService createPersistentUpdateMethodService();
}