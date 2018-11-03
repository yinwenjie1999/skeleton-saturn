package com.vanda.platform.saturn.core.scan;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.engine.annotation.BuildQueryMethods;
import com.vanda.platform.saturn.core.engine.annotation.BuildUpdateMethods;
import com.vanda.platform.saturn.core.engine.annotation.SaturnEntity;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnUpdateMethod;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.model.PersistentQueryMethod;
import com.vanda.platform.saturn.core.model.PersistentUpdateMethod;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;
import javassist.NotFoundException;

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
  
  private List<PersistentClass> persistentClasses = null;
  
  static {
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    classPool.appendClassPath(new LoaderClassPath(currentClassLoader));
  }
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.handler.SaturnHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  @Override
  public void handle(SaturnContext context) {
    String[] rootScanPackages = context.getRootScanPackages();
    Validate.isTrue(rootScanPackages != null && rootScanPackages.length > 0 , "请为骨架分析器指定扫描模型定义的根包路径（至少指定一个）");
    persistentClasses = this.scan(rootScanPackages);
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.ClassScanner#scan(java.lang.String)
   */
  @Override
  public List<PersistentClass> scan(String[] rootPackages) {
    if(rootPackages == null || rootPackages.length == 0) {
      return null;
    }
    Map<String, PersistentClass> persistentClassMapping = new LinkedHashMap<>();
    // 这个映射关系需要说明一下，这是一个经过字段扫描阶段，为每一个符合要求的静态模型定义生成的分析器对象
    Map<String, JavassistAnalysis> javassistAnalysisMapping = new LinkedHashMap<>();
    ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
    
    /*
     * 需要遍历两次，过程为：
     * 1、基于当前rootPackage，获得当前所有的url信息，这是第一级遍历的基础
     * 第一次遍历主要是为了分析符合要求的模型定义中的普通字段、关联字段
     * 2、在模型定义中的普通字段和关联字段都清楚了的情况下，才对模型定义中都可方法（包括更新方法、查询方法）
     * 进行分析
     * */
    for (String rootPackage : rootPackages) {
      String rootPackagePath = StringUtils.replaceAll(rootPackage, "\\.", "/");
      Enumeration<URL> urls = null;
      try {
        urls = currentClassLoader.getResources(rootPackagePath);
      } catch (IOException e) {
        LOGGER.warn(e.getMessage());
        continue;
      }
      
      // 1、====================开始遍历，对最终是要对所有符合模型规范的class文件的属性进行分析
      while(urls.hasMoreElements()) {
        URL classUrl = urls.nextElement();
        LOGGER.info("classUrl = " + classUrl);
        String protocol = classUrl.getProtocol();
        // 如果是以文件的形式保存在服务器上
        if ("file".equals(protocol)) {
          scanFileForFields(persistentClassMapping , javassistAnalysisMapping, classUrl.getPath() , rootPackage);
        } 
        // 否则就是jar包形式了
        else if("jar".equals(protocol)) {
          scanJarForFields(persistentClassMapping , javassistAnalysisMapping, classUrl);
        } 
      }
      
      // 2、==================然后再进行一次遍历，这次遍历是要对符合模型规范的class文件的方法定义进行分析
      Collection<PersistentClass> persistentClasses = persistentClassMapping.values();
      for (PersistentClass persistentClass : persistentClasses) {
        String className = persistentClass.getClassName();
        CtClass currentCtClass;
        try {
          currentCtClass = classPool.get(className);
        } catch (NotFoundException e) {
          LOGGER.error(e.getMessage());
          throw new IllegalArgumentException(e.getMessage());
        }
        scanClassMethods(currentCtClass, persistentClass , persistentClassMapping , javassistAnalysisMapping);
      }
    }
    
    return new LinkedList<>(persistentClassMapping.values());
  }
  
  /**
   * TODO 注释未写，但是有很重要，所以必须写
   * @param persistentClassMapping 当前已经建立好的模型完整类型和模型描述间的关系
   * @param javassistAnalysisMapping 这个映射关系需要说明一下，这是一个经过字段扫描阶段，为每一个符合要求的静态模型定义生成的分析器对象。后续方法分析过程需要这个映射关系
   * @param jarUrl jar文件的url对应位置
   */
  private void scanJarForFields(Map<String, PersistentClass> persistentClassMapping, Map<String, JavassistAnalysis> javassistAnalysisMapping ,URL jarUrl) {
    JarFile jar;
    try {
      jar = ((JarURLConnection) jarUrl.openConnection()).getJarFile();
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
      return;
    }
    Enumeration<JarEntry> entries = jar.entries();
    while(entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      String jarEntryName = jarEntry.getName();
      // 如果条件成立，说明这个文件不是可能需要的class
      if(!StringUtils.endsWith(jarEntryName, ".class") || StringUtils.contains(jarEntryName, "$")) {
        continue;
      }
      String classFullName = StringUtils.removeEnd(jarEntryName, ".class");
      classFullName = StringUtils.replaceAll(classFullName, "/", ".");
      
      // 可以加载class了
      CtClass currentCtClass = null;
      try {
        currentCtClass = classPool.get(classFullName);
      } catch (NotFoundException e) {
        LOGGER.warn(e.getMessage());
        continue;
      }
      
      // 根据class中可能的Entity和SaturnEntity确认要使用的分析工具（JavassistAnalysis的具体实现）
      boolean hasEntityAnnotation = currentCtClass.hasAnnotation(Entity.class);
      boolean hasSaturnEntityAnnotation = currentCtClass.hasAnnotation(SaturnEntity.class);
      PersistentClass persistentClass = null;
      JavassistAnalysis analysis = null;
      if(!hasEntityAnnotation && !hasSaturnEntityAnnotation) {
        continue;
      } else if(hasEntityAnnotation) {
        analysis = new JpaTagJavassistAnalysis();
        persistentClass = analysis.analyze(currentCtClass, null, null, false);
      } else if(!hasEntityAnnotation && hasSaturnEntityAnnotation) {
        analysis = new OwnerTagJavassistAnalysis();
        persistentClass = analysis.analyze(currentCtClass, null, null, false);
      }
      if(persistentClass != null) {
        persistentClassMapping.put(classFullName, persistentClass);
        javassistAnalysisMapping.put(classFullName, analysis);
      }
    }
  }
  
  /**
   * 扫描指定文件夹下的class文件信息，并找出来符合模型定义规范的class进行分析
   * @param persistentClassMapping
   * @param javassistAnalysisMapping
   * @param currentPackagePath
   * @param currentPackageName
   */
  private void scanFileForFields(Map<String, PersistentClass> persistentClassMapping , Map<String, JavassistAnalysis> javassistAnalysisMapping , String currentPackagePath , String currentPackageName) {
    File dir = new File(currentPackagePath);
    // 如果不存在或者 也不是目录就直接返回
    if (!dir.exists() || !dir.isDirectory()) {
      return;
    }
    File[] childfiles = dir.listFiles((File pathname) -> {
      return (pathname.isDirectory()) || (pathname.getName().endsWith(".class"));
    });
    
    for (File file : childfiles) {
      // 如果是目录 则继续扫描
      if (file.isDirectory()) {
        String childPackageName = file.getName();
        scanFileForFields(persistentClassMapping , javassistAnalysisMapping,  file.getAbsolutePath() , currentPackageName + "." + childPackageName);
      } else {
        String fileName = file.getName();
        LOGGER.debug("current fileName = " + fileName);
        // 只有class文件才符合处理条件，否则跳过即可
        if(!file.getName().endsWith(".class")) return;
        String fileSortName = StringUtils.removeEnd(fileName, ".class");
        CtClass currentCtClass = null;
        String classFullName = currentPackageName + "." + fileSortName;
        try {
          currentCtClass = classPool.get(classFullName);
        } catch (NotFoundException e) {
          LOGGER.error(e.getLocalizedMessage());
          continue;
        }
        
        // 根据class中可能的Entity和SaturnEntity确认要使用的分析工具（JavassistAnalysis的具体实现）
        // TODO 这里还有代码重复，需要私有方法封装
        boolean hasEntityAnnotation = currentCtClass.hasAnnotation(Entity.class);
        boolean hasSaturnEntityAnnotation = currentCtClass.hasAnnotation(SaturnEntity.class);
        PersistentClass persistentClass = null;
        JavassistAnalysis analysis = null;
        if(!hasEntityAnnotation && !hasSaturnEntityAnnotation) {
          continue;
        } else if(hasEntityAnnotation) {
          analysis = new JpaTagJavassistAnalysis();
          persistentClass = analysis.analyze(currentCtClass, null, null, false);
        } else if(!hasEntityAnnotation && hasSaturnEntityAnnotation) {
          analysis = new OwnerTagJavassistAnalysis();
          persistentClass = analysis.analyze(currentCtClass, null, null, false);
        }
        if(persistentClass != null) {
          persistentClassMapping.put(classFullName, persistentClass);
          javassistAnalysisMapping.put(classFullName, analysis);
        }
      }
    }
  }
  
  /**
   * 该方法用于分析处理指定class在其头部描述的自定义方法——包括自定义查询方法、自定义更新方法
   * @param currentCtClass 当前正在被分析的class文件的javassist对象表示
   * @param persistentClass 这个对象是已经完成上一步字段扫描后形成的该class的对象模型表示
   * @param javassistAnalysisMapping 之前已经完成的模型属性分析过程中，建立的模型与分析器间的关系
   * TODO 还需要定制的“插入”方法吗？目前看来可能不需要
   */
  private void scanClassMethods(CtClass currentCtClass , PersistentClass persistentClass ,Map<String, PersistentClass> persistentClassMapping, Map<String, JavassistAnalysis> javassistAnalysisMapping) {
    // 以下是定制的查询方法的处理过程
    boolean hasBuildQueryMethodsAnnotation = currentCtClass.hasAnnotation(BuildQueryMethods.class);
    String fullClassName = persistentClass.getClassName();
    List<PersistentQueryMethod> persistentQueryMethods = null;
    if(hasBuildQueryMethodsAnnotation) {
      Object[] methodsAnnotationObjects = null;
      try {
        methodsAnnotationObjects = currentCtClass.getAnnotations();
      } catch (ClassNotFoundException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException(e);
      }
      
      SaturnQueryMethod[] saturnQueryMethods = null;
      for(int index = 0 ; methodsAnnotationObjects != null && index < methodsAnnotationObjects.length ; index++) {
        Object annotationObject = methodsAnnotationObjects[index];
        // 如果条件成立，才说明他是一个自定义查询性质的描述
        if(annotationObject instanceof BuildQueryMethods) {
          BuildQueryMethods buildQueryMethods = (BuildQueryMethods)annotationObject;
          saturnQueryMethods = buildQueryMethods.methods();
          break;
        }
      }
      if(saturnQueryMethods != null) {
        persistentQueryMethods = new LinkedList<>();
        JavassistAnalysis currentAnalysis = javassistAnalysisMapping.get(fullClassName);
        Validate.notNull(currentAnalysis , "如果出现该错误说明程序工作异常，请提交patch(currentAnalysis is null[classname:" + fullClassName + "])");
        for (SaturnQueryMethod saturnQueryMethod : saturnQueryMethods) {
          PersistentQueryMethod persistentQueryMethod = currentAnalysis.analysisQueryMethod(persistentClass, saturnQueryMethod, persistentClassMapping);
          if(persistentQueryMethod != null) {
            persistentQueryMethods.add(persistentQueryMethod);
          }
        }
      }
    }
    persistentClass.setQueryMethods(persistentQueryMethods);
    
    // 以下是定制的修改方法
    // 注意修改方法中进行写操作的字段只能在本class（或其父类）存在，查询条件也是
    boolean hasBuildUpdateMethodsAnnotation = currentCtClass.hasAnnotation(BuildUpdateMethods.class);
    List<PersistentUpdateMethod> persistentUpdateMethods = null;
    if(hasBuildUpdateMethodsAnnotation) {
      Object[] methodsAnnotationObjects = null;
      try {
        methodsAnnotationObjects = currentCtClass.getAnnotations();
      } catch (ClassNotFoundException e) {
        LOGGER.error(e.getMessage());
        throw new IllegalArgumentException(e);
      }
      
      SaturnUpdateMethod[] saturnUpdateMethods = null;
      for(int index = 0 ; methodsAnnotationObjects != null && index < methodsAnnotationObjects.length ; index++) {
        Object annotationObject = methodsAnnotationObjects[index];
        // 如果条件成立，才说明他是一个自定义修改性质的描述
        if(annotationObject instanceof BuildUpdateMethods) {
          BuildUpdateMethods buildUpdateMethods = (BuildUpdateMethods)annotationObject;
          saturnUpdateMethods = buildUpdateMethods.methods();
          break;
        }
      }
      if(saturnUpdateMethods != null) {
        persistentUpdateMethods = new LinkedList<>();
        JavassistAnalysis currentAnalysis = javassistAnalysisMapping.get(fullClassName);
        Validate.notNull(currentAnalysis , "如果出现该错误说明程序工作异常，请提交patch(currentAnalysis is null[classname:" + fullClassName + "])");
        for (SaturnUpdateMethod saturnUpdateMethod : saturnUpdateMethods) {
          PersistentUpdateMethod persistentUpdateMethod = currentAnalysis.analysisUpdateMethod(persistentClass, saturnUpdateMethod);
          if(persistentUpdateMethod != null) {
            persistentUpdateMethods.add(persistentUpdateMethod);
          }
        }
      }
    }
    persistentClass.setUpdateMethods(persistentUpdateMethods);
  }

  public List<PersistentClass> getPersistentClasses() {
    return persistentClasses;
  }
}