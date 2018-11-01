package com.vanda.platform.saturn.core.context.service;

import java.util.List;
import java.util.Map;

import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * 和PersistentClass对象相关的数据层操作在这里进行。
 * @author yinwenjie
 */
public interface PersistentClassService {
  
  /**
   * 该方法用于基于特定的存储技术，将已经扫描完成的（或者是再次扫描完成的）的持久层数据进行存储，包括PersistentClass的各种关联信息
   * @param persistentMappingClasses
   */
  public void refreshAllPersistent(Map<String , PersistentClass> persistentMappingClasses);
  
  /**
   * 保存一个class模型定义，但是不包括其关联的诸如：persistentProperty、persistentQueryMethod、persistentRelation等信息
   * @param persistentClass
   */
  public void save(PersistentClass persistentClass);
  /**
   * 按照指定的模型完整类名，删除这个模型在内存中的存储
   * @param className
   */
  public void delete(String className);
  /**
   * 该方法用于查询指定的完整类名所代表的PersistentClass的全部信息，不包括关联信息
   * @param className 指定的完整class的名字
   * @return 
   */
  public PersistentClass queryByClassName(String className);
  
  /**
   * 返回指定域下的多个PersistentClass信息，但是不包括这些模型类定义信息的关联信息
   * @param domainName 指定的域信息
   * @return 
   */
  public List<PersistentClass> queryByDomainName(String domainName);
}