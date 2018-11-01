package com.vanda.platform.saturn.core.context.repository;

import java.util.List;

import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * 和PersistentClass对象相关的数据层操作在这里进行。
 * @author yinwenjie
 */
public interface PersistentClassRepository {
  
  /**
   * 保存一个class模型定义，包括其可能的关联信息
   * @param persistentClass
   */
  public void save(PersistentClass persistentClass);
  
  /**
   * 该方法用于查询指定的完整类名所代表的PersistentClass的全部信息，包括关联信息
   * @param className 指定的完整class的名字
   * @return 
   */
  public PersistentClass queryByClassName(String className);
  
  /**
   * 返回指定域下的多有PersistentClass信息，以及他们的关联信息
   * @param domainName 指定的域信息
   * @return
   */
  public List<PersistentClass> queryByDomainName(String domainName);
}