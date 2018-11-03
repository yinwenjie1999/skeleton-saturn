package com.vanda.platform.saturn.core.test.scan;

import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.context.SimpleSaturnContext;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.scan.SimpleClassScanner;

/**
 * 针对SimpleClassScanner关键类的一般性测试用例<br>
 * TODO "@SaturnQueryMethod"注解下的多级属性关联还没有测试
 * @author yinwenjie
 */
public class SimpleClassScannerTest {
  private SimpleClassScanner simpleClassScanner;
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClassScannerTest.class);
  
  static {
    BasicConfigurator.configure();
  }
  
  @Before
  public void beforeTest() {
    
  }
  /**
   * 测试基于javassist技术和基于普通文件系统进行的扫描
   */
  @Test
  public void javassistScanTestForFile() {
    String[] rootScanPackages = new String[]{"com.vanda.platform"};
    SaturnContext saturnContext = new SimpleSaturnContext();
    saturnContext.setRootScanPackages(rootScanPackages);
    
    simpleClassScanner = new SimpleClassScanner();
    // 开始进行扫描
    simpleClassScanner.handle(saturnContext);
    List<PersistentClass> persistentClasses = simpleClassScanner.getPersistentClasses();
    // 得到的persistentClasses;
    LOGGER.info("persistentClasses = " + persistentClasses);
  }
  
  /**
   * 测试基于javassist技术和jar压缩文件进行的扫描
   * TODO 没有测试
   */
  @Test
  public void javassistScanTestForJar() {
    
  }
}