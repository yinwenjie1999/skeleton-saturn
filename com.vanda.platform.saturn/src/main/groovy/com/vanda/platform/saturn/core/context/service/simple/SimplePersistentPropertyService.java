package com.vanda.platform.saturn.core.context.service.simple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vanda.platform.saturn.core.context.service.PersistentPropertyService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentProperty;

/**
 * TODO 注释未写
 * @author yinwenjie
 */
public class SimplePersistentPropertyService implements PersistentPropertyService {
  
  private Map<String, PersistentClass> persistentClassMapping;
  
  SimplePersistentPropertyService(Map<String, PersistentClass> persistentClassMapping) {
    this.persistentClassMapping = persistentClassMapping;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentPropertyService#save(com.vanda.platform.saturn.core.model.PersistentProperty)
   */
  @Override
  public void save(PersistentProperty property) {
    Validate.notNull(property , "[save]，错误的property信息，请检查!!");
    String persistentClassName = property.getPersistentClassName();
    Validate.notBlank(persistentClassName , "必须指定模型类的全名，请检查!!");
    String propertyName = property.getPropertyName();
    Validate.notBlank(propertyName , "必须指定属性名");
    String propertyClass = property.getPropertyClass();
    Validate.notBlank(propertyClass , "必须指定属性类型");
    
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
    List<PersistentProperty> properties = persistentClass.getProperties();
    if(properties == null) {
      properties = new LinkedList<>();
      persistentClass.setProperties(properties);
    }
    int index = 0;
    boolean found = false;
    for (; index < properties.size() ; index++) {
      PersistentProperty propertyItem = properties.get(index);
      if(StringUtils.equals(propertyName, propertyItem.getPropertyName())) {
        found = true;
        break;
      }
    }
    if(found) {
      properties.set(index, property);
    } else {
      properties.add(property);
    }
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentPropertyService#findByClassName(java.lang.String)
   */
  @Override
  public Map<String, PersistentProperty> findByClassName(String className) {
    if(StringUtils.isBlank(className)) {
      return null;
    }
    // TODO 代码存在较高重复度
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    List<PersistentProperty> properties = persistentClass.getProperties();
    if(properties == null || properties.isEmpty()) {
      return null;
    }
    
    // 转换成map映射
    Map<String, PersistentProperty> propertyMapping = null;
    propertyMapping = properties.stream().collect(Collectors.toMap(PersistentProperty::getPropertyName, persistentProperty -> persistentProperty));
    return propertyMapping;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentPropertyService#findPrimaryKey(java.lang.String)
   */
  @Override
  public PersistentProperty findPrimaryKey(String className) {
    if(StringUtils.isBlank(className)) {
      return null;
    }
    
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    List<PersistentProperty> properties = persistentClass.getProperties();
    if(properties == null || properties.isEmpty()) {
      return null;
    }
    
    List<PersistentProperty> primaryKeys = properties.stream().filter(item -> item.getPrimaryKey()).collect(Collectors.toList());
    if(primaryKeys == null || primaryKeys.isEmpty()) {
      return null;
    }
    return primaryKeys.get(0);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentPropertyService#findByPropertyName(java.lang.String, java.lang.String)
   */
  @Override
  public PersistentProperty findByPropertyName(String className, String propertyName) {
    if(StringUtils.isBlank(className) || StringUtils.isBlank(propertyName)) {
      return null;
    }
    // TODO 代码存在较高重复度
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    List<PersistentProperty> properties = persistentClass.getProperties();
    if(properties == null || properties.isEmpty()) {
      return null;
    }
    
    for (PersistentProperty propertyItem : properties) {
      if(StringUtils.equals(propertyName, propertyItem.getPropertyName()))
        return propertyItem;
    }
    return null;
  }
}