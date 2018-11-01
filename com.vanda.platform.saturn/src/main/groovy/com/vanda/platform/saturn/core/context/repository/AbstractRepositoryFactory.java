package com.vanda.platform.saturn.core.context.repository;

/**
 * 数据存储层对象的抽象工厂。保证了不同的数据存储层能够通过不同的逻辑被初始化
 * @author yinwenjie
 */
public abstract class AbstractRepositoryFactory {
  /**
   * 创建为“模型类”描述服务的存储层实现
   * @return
   */
  public abstract PersistentClassRepository createPersistentClassRepository();

  /**
   * 创建为“模型类中一般属性”描述服务的存储层实现
   * @return
   */
  public abstract PersistentPropertyRepository createPersistentPropertyRepository();
  
  /**
   * 创建为“模型类中一般属性”描述服务的存储层实现
   * @return
   */
  public abstract PersistentRelationRepository createPersistentRelationRepository();
  
  /**
   * 创建为“模型类中自定义查询方法的”描述服务的存储层实现
   * @return
   */
  public abstract PersistentQueryMethodRepository createPersistentQueryMethodRepository();
  
  /**
   * 创建为“模型类中自定义更新方法的”描述服务的存储层实现
   * @return
   */
  public abstract PersistentUpdateMethodRepository createPersistentUpdateMethodRepository();
}