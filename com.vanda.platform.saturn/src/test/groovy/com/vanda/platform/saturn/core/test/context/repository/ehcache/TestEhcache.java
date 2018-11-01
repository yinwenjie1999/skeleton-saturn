package com.vanda.platform.saturn.core.test.context.repository.ehcache;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.test.entity.UserEntity;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.Configuration;

/**
 * 对ehcache的基本使用方式进行示例
 * @author yinwenjie
 */
public class TestEhcache {
  
  private Cache cache = null;
  
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(TestEhcache.class);
  
  @Before
  public void testBefore() {
    if(cache != null) {
      return;
    }
    synchronized (TestEhcache.class) {
      while(cache != null) {
        try {
          TestEhcache.class.wait();
        } catch (InterruptedException e) {
          LOGGER.error(e.getMessage() , e);
          return;
        }
      }
      
      Configuration configuration = new Configuration();
      // 用来限制缓存所能使用的堆内存的最大字节数的，默认为0，表示不限制
      configuration.setMaxBytesLocalHeap("200M");
      // 是用来限制缓存所能使用的非堆内存的最大字节数，默认为0，表示不限制
      configuration.setMaxBytesLocalOffHeap("0");
      // 是用来限制缓存所能使用的磁盘的最大字节数的，默认为0，表示不限制
      configuration.setMaxBytesLocalDisk("0");
      
      // 默认的cache级别的配置
      CacheConfiguration cacheConfiguration = new CacheConfiguration();
      // 使用copyOnWrite模式
      cacheConfiguration.setCopyOnWrite(true);
      // 缓存中的对象永生，否则就要设定TimeToXXXX属性
      cacheConfiguration.setEternal(false);
      // 对象空闲时，指对象在多长时间没有被访问就会失效。只对eternal为false的有效。默认值为0。
      // cacheConfiguration.setTimeToIdleSeconds(0);
      // cacheConfiguration.setTimeToLiveSeconds(0);
      configuration.setDefaultCacheConfiguration(cacheConfiguration);
      CacheManager cacheManager = CacheManager.create(configuration);
      cache = cacheManager.getCache("persistentClasses");
      TestEhcache.class.notify();
    }
  }
  
  @Test
  public void testEhcache() {
    // 设置要存储的对象
    UserEntity user = new UserEntity();
    user.setAccount("yinwenjie1999");
    user.setCreateTime(new Date());
    user.setGender(1);
    user.setId("id1");
    user.setListsort(100);
    user.setPassword("123456");
    user.setUserHead("");
    user.setUserName("yinwenjei1999");
    
    cache.put(new Element(user.getId(), user));
  }
}