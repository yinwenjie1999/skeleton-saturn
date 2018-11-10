package com.vanda.platform.saturn.core.context.service.simple;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

/**
 * TODO 注释未写
 * @author yinwenjie
 */
public class SimpleUpdateMethodService implements PersistentUpdateMethodService {
  private Map<String, PersistentClass> persistentClassMapping;
  
  // TODO 这里的public只是为了跑单元测试，正式发布要去掉
  public SimpleUpdateMethodService(Map<String, PersistentClass> persistentClassMapping) {
    this.persistentClassMapping = persistentClassMapping;
  } 
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService#save(com.vanda.platform.saturn.core.model.PersistentUpdateMethod)
   */
  @Override
  public void save(PersistentUpdateMethod updateMethod) {
    Validate.notNull(updateMethod , "新增的方法信息必须传入!!");
    String description = updateMethod.getDescription();
    Validate.notBlank(description , "方法描述信息必须传入!!");
    String persistentClassName = updateMethod.getPersistentClassName();
    Validate.notBlank(persistentClassName , "必须指定模型类的全名，请检查!!");
    PersistentClass persistentClass = this.persistentClassMapping.get(persistentClassName);
    Validate.notNull(persistentClass , "没有找到指定的类模型[" + persistentClassName + "]");
    String[] updateParams = updateMethod.getUpdateParams();
    Validate.isTrue(updateParams != null && updateParams.length > 0 , "请至少指定一个更新参数信息!!");
    String[] queryParams = updateMethod.getQueryParams();
    Validate.isTrue(queryParams != null && queryParams.length > 0 , "请至少指定一个条件参数信息!!");
    String methodName = updateMethod.getMethodName();
    Validate.isTrue(StringUtils.startsWith(methodName, "update") , "自定义更新方法必须以update关键字开始!!");
    
    // 确认这个方法是否已经存在于集合中
    List<PersistentUpdateMethod> updateMethods = persistentClass.getUpdateMethods();
    if(updateMethods == null) {
      updateMethods = new LinkedList<>();
      persistentClass.setUpdateMethods(updateMethods);
    }
    // 方法名一致时就认为是重复的方法了
    List<String> currentMethodNames = updateMethods.stream().map(PersistentUpdateMethod::getMethodName).collect(Collectors.toList());
    int foundIndex = currentMethodNames.indexOf(methodName);
    if(foundIndex != -1) {
      updateMethods.set(foundIndex, updateMethod);
    } else {
      updateMethods.add(updateMethod);
    }
  }
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService#queryByClassName(java.lang.String)
   */
  @Override
  public List<PersistentUpdateMethod> queryByClassName(String className) {
    if(StringUtils.isBlank(className)) {
      return null;
    }
    PersistentClass persistentClass = this.persistentClassMapping.get(className);
    if(persistentClass == null) {
      return null;
    }
    
    return persistentClass.getUpdateMethods();
  }
}
