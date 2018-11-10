package com.vanda.platform.saturn.core.context.service.simple;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.vanda.platform.saturn.core.context.service.PersistentClassService;
import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * TODO 未写注释
 * @author yinwenjie
 */
public class SimplePersistentClassService implements PersistentClassService {

  private Map<String, PersistentClass> persistentClassMapping;
  
  // TODO 这里的public只是为了跑单元测试，正式发布要去掉
  public SimplePersistentClassService(Map<String, PersistentClass> persistentClassMapping) {
    this.persistentClassMapping = persistentClassMapping;
  }
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentClassService#refreshAllPersistent(java.util.Map)
   */
  @Override
  public void refreshAllPersistent(Map<String, PersistentClass> persistentMappingClasses) {
    // TODO 还没有写
  }

  @Override
  public void save(PersistentClass persistentClass) {
    /*
     * 判断重点属性，只要有就放进去。当前有在用的也不管
     * */
    Validate.notNull(persistentClass , "persistentClass not be null!!");
    String clssName = persistentClass.getClassName();
    Validate.notBlank(clssName , "clssName not be null");
    persistentClassMapping.put("clssName", persistentClass);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentClassService#delete(java.lang.String)
   */
  @Override
  public void delete(String className) {
    if(StringUtils.isBlank(className)) {
      return;
    }
    
    this.persistentClassMapping.remove(className);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentClassService#queryAllClasses()
   */
  @Override
  public List<PersistentClass> queryAllClasses() {
    return new LinkedList<>(this.persistentClassMapping.values());
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.repository.PersistentClassRepository#queryByClassName(java.lang.String)
   */
  @Override
  public PersistentClass queryByClassName(String className) {
    return persistentClassMapping.get(className);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.repository.PersistentClassRepository#queryByDomainName(java.lang.String)
   */
  @Override
  public List<PersistentClass> queryByDomainName(String domainName) {
    if(StringUtils.isBlank(domainName)) {
      return null;
    }
    List<PersistentClass> domainPersistentClasses = new LinkedList<>();
    Collection<PersistentClass> classes = persistentClassMapping.values();
    
    for (PersistentClass persistentClass : classes) {
      String domain = persistentClass.getDomain();
      if(StringUtils.equals(domain, domainName)) {
        domainPersistentClasses.add(persistentClass);
      }
    }
    return domainPersistentClasses;
  }
}