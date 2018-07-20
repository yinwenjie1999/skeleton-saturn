package com.vanda.platform.saturn.core.context.repository.simple;

import java.util.List;
import java.util.Map;

import com.vanda.platform.saturn.core.context.repository.PersistentClassRepository;
import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * TODO 注释未写
 * @author yinwenjie
 *
 */
public class SimplePersistentClassRepository implements PersistentClassRepository {

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.repository.PersistentClassRepository#refreshAllPersistentClass(java.util.Map)
   */
  @Override
  public void refreshAllPersistentClass(Map<String, PersistentClass> persistentMappingClasses) {
    // TODO Auto-generated method stub
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.repository.PersistentClassRepository#queryByClassName(java.lang.String)
   */
  @Override
  public PersistentClass queryByClassName(String className) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.repository.PersistentClassRepository#queryByDomainName(java.lang.String)
   */
  @Override
  public List<PersistentClass> queryByDomainName(String domainName) {
    // TODO Auto-generated method stub
    return null;
  }

}
