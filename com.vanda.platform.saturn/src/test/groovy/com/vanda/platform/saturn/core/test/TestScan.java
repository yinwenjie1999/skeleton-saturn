package com.vanda.platform.saturn.core.test;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;

public class TestScan {
  private static String rootProject = "E:/groovy-workspace/com.vanda.platform.saturn.test";
  private static String currentProjectName = "com.vanda.platform.saturn.test";
  private static String entityProjectName = "com.vanda.platform.saturn.test.entity";
  private static String classRootName = "bin";
  private static String javaRootName = "src/main/java";
  
  
  public static void main(String[] args) throws Exception {
    String classRootDir = "file:" + rootProject + "\\" + entityProjectName + "\\" + classRootName + "\\";
    URL classRootDirUrl = new URL(classRootDir);
    ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();
    ClassLoader currentClassLoader = URLClassLoader.newInstance(new URL[]{classRootDirUrl} , parentClassLoader);
    Class<?> currentClass = currentClassLoader.loadClass("com.vanda.platform.saturn.test.entity.AlarmEventEntity");
  }
}