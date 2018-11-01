package com.vanda.platform.saturn.core.context.service;

import java.util.Map;

import com.vanda.platform.saturn.core.model.PersistentRelation;

public interface PersistentRelationService {
  /**
   * 保存一个关联属性，如果存储层已经有了这个关联属性信息，则进行更新；如果没有则新建保存
   * @param property
   */
  public void save(PersistentRelation persistentRelation);
  /**
   * 通过一个模型类的完整名字，查询这个模型类下的属性描述——关联性属性。并且查询结果将按照属性中的index属性进行排序
   * @param className 指定的模型类完整类名
   * @return 返回信息的key，就是这些属性的完整属性名
   */
  public Map<String , PersistentRelation> queryByClassName(String className);
  
  /**
   * 按照模型类完整名称和属性完整名称，查询这个关联属性。 
   * @param className 模型类的完整类名 
   * @param propertyName 完整属性名 
   * @return 如果查询到则进行返回，其它情况返回null 
   */
  public PersistentRelation findByPropertyName(String className , String propertyName);
}
