package com.vanda.platform.saturn.core.context.service.simple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod;

/**
 * @author yinwenjie
 */
public class SimpleQueryMethodService implements PersistentQueryMethodService {
  private Map<String, PersistentClass> persistentClassMapping;
  
  SimpleQueryMethodService(Map<String, PersistentClass> persistentClassMapping) {
    this.persistentClassMapping = persistentClassMapping;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService#save(com.vanda.platform.saturn.core.model.PersistentQueryMethod)
   */
  @Override
  public void save(PersistentQueryMethod queryMethod) {
    Validate.notNull(queryMethod , "新增的方法信息必须传入!!");
    String description = queryMethod.getDescription();
    Validate.notBlank(description , "方法描述信息必须传入!!");
    String persistentClassName = queryMethod.getPersistentClassName();
    Validate.notBlank(persistentClassName , "必须指定模型类的全名，请检查!!");
    PersistentClass persistentClass = this.persistentClassMapping.get(persistentClassName);
    Validate.notNull(persistentClass , "没有找到指定的类模型[" + persistentClassName + "]");
    String[] params = queryMethod.getParams();
    Validate.isTrue(params != null && params.length > 0 , "请至少指定一个参数信息!!");
    
    // 确认这个方法是否已经存在于集合中
    List<PersistentQueryMethod> queryMethods = persistentClass.getQueryMethods();
    if(queryMethods == null) {
      queryMethods = new LinkedList<>();
      persistentClass.setQueryMethods(queryMethods);
    }
    int index = 0;
    boolean found = false;
    for (; index < queryMethods.size() ; index++) {
      PersistentQueryMethod queryMethodItem = queryMethods.get(index);
      // 只有所有的参数都一致，才认为是重复的方法
      // TODO 未完成
    }
    if(found) {
      queryMethods.set(index, queryMethod);
    } else {
      queryMethods.add(queryMethod);
    }
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService#queryByClassName(java.lang.String)
   */
  @Override
  public List<PersistentQueryMethod> queryByClassName(String className) {
    if(StringUtils.isBlank(className)) {
      return null;
    }
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    
    return persistentClass.getQueryMethods();
  }
}
