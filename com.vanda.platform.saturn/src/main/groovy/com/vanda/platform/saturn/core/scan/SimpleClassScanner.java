package com.vanda.platform.saturn.core.scan;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.gradle.internal.impldep.aQute.bnd.osgi.Clazz;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.model.PersistentClass;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

/**
 * 基于class文件扫描的默认实现
 * @author yinwenjie
 */
public class SimpleClassScanner implements ClassScanner {

  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleClassScanner.class);
  
  private static ClassPool classPool = ClassPool.getDefault();
  
  static {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    classPool.appendClassPath(new LoaderClassPath(currentClassLoader));
  }
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.handler.SaturnHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  @Override
  public void handle(SaturnContext context) {
    // TODO 这里还没有从上下文会话中取出
    String projectRootPackage = "";
    this.scan(projectRootPackage);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.ClassScanner#scan(java.lang.String)
   */
  @Override
  public List<PersistentClass> scan(String projectRootPackage) {
    List<PersistentClass> persistentClasses = new LinkedList<>();
    /*
     * TODO 核心的扫描过程，必须进行注释说明
     * */
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    Enumeration<URL> dirs = null;
    try {
      dirs = currentClassLoader.getResources(projectRootPackage);
    } catch(IOException e) {
      LOGGER.error(e.getMessage() , e);
      throw new IllegalArgumentException(e);
    }
    
    while (dirs.hasMoreElements()) {
      URL url = dirs.nextElement();
      // 得到协议的名称
      String protocol = url.getProtocol();
      // 如果是以文件的形式保存在服务器上
      if (StringUtils.equals("file", protocol)) {
        String filePath = null;
        try {
          filePath = URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
          throw new IllegalArgumentException(e);
        }
        // 以文件的方式扫描整个包下的文件 并添加到集合中
        classAnalysisForFile(filePath);
      } else if (StringUtils.equals("jar", protocol)) {
        
      }
    }
    
    return null;
  }
  
  /**
   * @param filePath
   * @return
   */
  private PersistentClass classAnalysisForFile(String packagePath) {
    // 获取此包的目录 建立一个File
    File dir = new File(packagePath);

    // 如果不存在或者 也不是目录就直接返回
    if (!dir.exists() || !dir.isDirectory()) {
      return null;
    }
    File[] dirfiles = dir.listFiles((File pathname) -> {
      return (pathname.isDirectory()) || (pathname.getName().endsWith(".class"));
    });

    // 循环所有文件
    for (File file : dirfiles) {
      // 如果是目录 则继续扫描
      if (file.isDirectory()) {
        classAnalysisForFile(file.getAbsolutePath());
      } else {
//        ClassPool classPool = ClassPool.getDefault();
//        classPool.get
//        CtClass ctClass = classPool.get("eerer");
//        ctClass.getFields()
//        
//        classPool.appendClassPath(cp)
        
        // 如果是java类文件 去掉后面的.class 只留下类名
        String className = file.getName().substring(0, file.getName().length() - 6);
        
      }
    }

    return null;
  }
}