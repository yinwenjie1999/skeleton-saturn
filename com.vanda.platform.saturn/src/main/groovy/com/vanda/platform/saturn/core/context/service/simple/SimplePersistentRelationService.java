package com.vanda.platform.saturn.core.context.service.simple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vanda.platform.saturn.core.context.service.PersistentRelationService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentRelation;
import com.vanda.platform.saturn.core.model.PersistentRelation.RelationType;

/**
 * TODO 未写注释
 * @author yinwenjie
 */
public class SimplePersistentRelationService implements PersistentRelationService {
  
  private Map<String, PersistentClass> persistentClassMapping;
  
  SimplePersistentRelationService(Map<String, PersistentClass> persistentClassMapping) {
    this.persistentClassMapping = persistentClassMapping;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentRelationService#save(com.vanda.platform.saturn.core.model.PersistentRelation)
   */
  @Override
  public void save(PersistentRelation persistentRelation) {
    Validate.notNull(persistentRelation , "[save]，错误的relation信息，请检查!!");
    String persistentClassName = persistentRelation.getPersistentClassName();
    Validate.notBlank(persistentClassName , "必须指定模型类的全名，请检查!!");
    String propertyName = persistentRelation.getPropertyName();
    Validate.notBlank(propertyName , "必须指定属性名");
    String propertyClass = persistentRelation.getPropertyClass();
    Validate.notBlank(propertyClass , "必须指定属性类型!!");
    RelationType relationType = persistentRelation.getRelationType();
    Validate.notNull(relationType , "必须指定关联类型!!");
    
    /*
     * 0、验证，在上面
     * 1、首先根据property中的类名找到这个类模型
     * 2、然后根据类模型找到字段信息
     * 2.1、如果找到了字段信息，则进行更新操作
     * 2.2、如果没有找到，则进行新增操作
     * */
    // 1、==========
    PersistentClass persistentClass = this.persistentClassMapping.get(persistentClassName);
    Validate.notNull(persistentClass , "没有找到指定的类模型[" + persistentClassName + "]");
    
    // 2、==========
    List<PersistentRelation> relations = persistentClass.getRelations();
    if(relations == null) {
      relations = new LinkedList<>();
      persistentClass.setRelations(relations);
    }
    int index = 0;
    boolean found = false;
    for (; index < relations.size() ; index++) {
      PersistentRelation relationItem = relations.get(index);
      if(StringUtils.equals(propertyName, relationItem.getPropertyName())) {
        found = true;
        break;
      }
    }
    if(found) {
      relations.set(index, persistentRelation);
    } else {
      relations.add(persistentRelation);
    }
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentRelationService#queryByClassName(java.lang.String)
   */
  @Override
  public Map<String , PersistentRelation> queryByClassName(String className) {
    if(StringUtils.isBlank(className)) {
      return null;
    }
    // TODO 代码存在较高重复度
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    List<PersistentRelation> relations = persistentClass.getRelations();
    if(relations == null || relations.isEmpty()) {
      return null;
    }
    
    // 转换成map映射
    Map<String, PersistentRelation> relationMapping = null;
    relationMapping = relations.stream().collect(Collectors.toMap(PersistentRelation::getPropertyName, persistentRelation -> persistentRelation));
    return relationMapping;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentRelationService#findByPropertyName(java.lang.String, java.lang.String)
   */
  @Override
  public PersistentRelation findByPropertyName(String className, String propertyName) {
    if(StringUtils.isBlank(className) || StringUtils.isBlank(propertyName)) {
      return null;
    }
    
    // TODO 代码存在较高重复度
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    List<PersistentRelation> relations = persistentClass.getRelations();
    if(relations == null || relations.isEmpty()) {
      return null;
    }
    
    for (PersistentRelation relationItem : relations) {
      if(StringUtils.equals(propertyName, relationItem.getPropertyName()))
        return relationItem;
    }
    return null;
  }
}
