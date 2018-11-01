package com.vanda.platform.saturn.core.context.service.simple;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.repository.AbstractRepositoryFactory;
import com.vanda.platform.saturn.core.context.repository.PersistentClassRepository;
import com.vanda.platform.saturn.core.context.repository.PersistentPropertyRepository;
import com.vanda.platform.saturn.core.context.repository.PersistentQueryMethodRepository;
import com.vanda.platform.saturn.core.context.repository.PersistentRelationRepository;
import com.vanda.platform.saturn.core.context.repository.PersistentUpdateMethodRepository;
import com.vanda.platform.saturn.core.context.service.PersistentClassService;
import com.vanda.platform.saturn.core.context.service.PersistentPropertyService;
import com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService;
import com.vanda.platform.saturn.core.context.service.PersistentRelationService;
import com.vanda.platform.saturn.core.context.service.PersistentServiceFactory;
import com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.test.context.repository.ehcache.TestEhcache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

/**
 * 该工厂专门生产基于普通内存对象的存储层操作对象<br>
 * 注意，整个core包中没有spring组件和缓存控制规范，所以只有自己写
 * @author yinwenjie
 */
public class SimpleRepositoryFactory extends PersistentServiceFactory {
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleRepositoryFactory.class);
  
  /**
   * TODO 未写注释
   */
  private static Map<String, PersistentClass> persistentClassMapping = new ConcurrentHashMap<>();
  /**
   * 简单的普通模型对象的内存存储模式，不需要带任何参数信息
   * @param maxBytesLocalHeap
   */
  public SimpleRepositoryFactory() {
    
  }
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentServiceFactory#createPersistentClassService()
   */
  @Override
  public PersistentClassService createPersistentClassService() {
    return new SimplePersistentClassService(persistentClassMapping);
  }
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentServiceFactory#createPersistentPropertyService()
   */
  @Override
  public PersistentPropertyService createPersistentPropertyService() {
    // TODO Auto-generated method stub
    return null;
  }
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentServiceFactory#createPersistentRelationService()
   */
  @Override
  public PersistentRelationService createPersistentRelationService() {
    // TODO Auto-generated method stub
    return null;
  }
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentServiceFactory#createPersistentQueryMethodService()
   */
  @Override
  public PersistentQueryMethodService createPersistentQueryMethodService() {
    // TODO Auto-generated method stub
    return null;
  }
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.context.service.PersistentServiceFactory#createPersistentUpdateMethodService()
   */
  @Override
  public PersistentUpdateMethodService createPersistentUpdateMethodService() {
    // TODO Auto-generated method stub
    return null;
  }
}