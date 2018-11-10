package com.vanda.platform.saturn.core.test.engine.handler;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.context.SimpleSaturnContext;
import com.vanda.platform.saturn.core.context.service.PersistentClassService;
import com.vanda.platform.saturn.core.context.service.PersistentPropertyService;
import com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService;
import com.vanda.platform.saturn.core.context.service.PersistentRelationService;
import com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService;
import com.vanda.platform.saturn.core.context.service.simple.SimplePersistentClassService;
import com.vanda.platform.saturn.core.context.service.simple.SimplePersistentPropertyService;
import com.vanda.platform.saturn.core.context.service.simple.SimplePersistentRelationService;
import com.vanda.platform.saturn.core.context.service.simple.SimpleQueryMethodService;
import com.vanda.platform.saturn.core.context.service.simple.SimpleUpdateMethodService;
import com.vanda.platform.saturn.core.engine.handler.HibernateJpaRepositoryGenerateHandler;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.scan.SimpleClassScanner;

/**
 * 对HibernateJpaRepositoryGenerateHandler处理器进行测试<br>
 * TODO HibernateJpaRepositoryGenerateHandler,自定义关联查询的过程还没有测试，其它都已经测试了
 * @author yinwenjie
 */
public class TestHibernateJpaRepositoryGenerateHandler {
  
  private SaturnContext saturnContext = new SimpleSaturnContext();
  
  @Before
  public void before() {
    /*
     * 这里主要是要完成context的初始化过程等。
     * 
     * */
    // 1、=====完成扫描
    String[] rootScanPackages = new String[]{"com.vanda.platform.saturn.core.test"};
    saturnContext.setRootScanPackages(rootScanPackages);
    SimpleClassScanner simpleClassScanner = new SimpleClassScanner();
    // 开始进行扫描
    simpleClassScanner.handle(saturnContext);
    List<PersistentClass> persistentClasses = simpleClassScanner.getPersistentClasses();
    Map<String , PersistentClass> persistentClassMapping = persistentClasses.stream().collect(Collectors.toMap(PersistentClass::getClassName, persistentClass -> persistentClass));
    
    // 2、====设定上下文
    PersistentClassService persistentClassService = new SimplePersistentClassService(persistentClassMapping);
    PersistentPropertyService persistentPropertyService = new SimplePersistentPropertyService(persistentClassMapping);
    PersistentQueryMethodService persistentQueryMethodService = new SimpleQueryMethodService(persistentClassMapping);
    PersistentRelationService persistentRelationService = new SimplePersistentRelationService(persistentClassMapping);
    PersistentUpdateMethodService persistentUpdateMethodService = new SimpleUpdateMethodService(persistentClassMapping);
    saturnContext.setPersistentClassService(persistentClassService);
    saturnContext.setPersistentPropertyService(persistentPropertyService);
    saturnContext.setPersistentQueryMethodService(persistentQueryMethodService);
    saturnContext.setPersistentRelationService(persistentRelationService);
    saturnContext.setPersistentUpdateMethodService(persistentUpdateMethodService);
    // 其它地址性质的配置信息
    String rootPackage = "com.vanda.platform.saturn.core.test";
    String projectAbsolutePath = "C:\\Users\\Administrator\\git\\skeleton-saturn\\com.vanda.platform.saturn";
    String projectSrcPath = "src/test/groovy";
    saturnContext.setRootPackage(rootPackage);
    saturnContext.setProjectAbsolutePath(projectAbsolutePath);
    saturnContext.setProjectSrcPath(projectSrcPath);
  }
  
  /**
   * 生成一个简单的数据层java
   */
  @Test
  public void simpleBuildJava() {
    HibernateJpaRepositoryGenerateHandler handler = new HibernateJpaRepositoryGenerateHandler();
    handler.handle(saturnContext);
  }
}