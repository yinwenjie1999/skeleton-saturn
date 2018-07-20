package com.vanda.platform.saturn.core.context.repository.h2;

import java.util.List;
import java.util.Map;

import com.vanda.platform.saturn.core.context.repository.PersistentClassRepository;
import com.vanda.platform.saturn.core.model.PersistentClass;

public class H2PersistentClassRepository implements PersistentClassRepository {

  @Override
  public void refreshAllPersistentClass(Map<String, PersistentClass> persistentMappingClasses) {
    // TODO Auto-generated method stub

  }

  @Override
  public PersistentClass queryByClassName(String className) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PersistentClass> queryByDomainName(String domainName) {
    // TODO Auto-generated method stub
    return null;
  }

}
