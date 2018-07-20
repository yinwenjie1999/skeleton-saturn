package com.vanda.platform.saturn.core.utils;

import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

/**
 * 基于eclipse开源JDT组件的AST实现工具。整个系统中只有这一个工具，
 * 且使用可重入锁进行线程安全控制；使用令牌机制进行AST一致性控制<br>
 * 为什么整个系统中只有一个JDT工具呢？这是因为AST初始化速度非常慢，过多的AST对象将显著降低运行效率。<br>
 * TODO 后期有需要可引入对象池的概念，改成多个ast对象
 * @author yinwenjie
 */
public class JdtUtils {
  private static ReentrantLock rol = new ReentrantLock();
  
  /**
   * 支持JDK10及以下版本
   */
  private static ASTParser parser = ASTParser.newParser(AST.JLS10);
  
  private JdtUtils() {
    
  }
  
  /**
   * 获取当前系统中唯一的一个AST抽象语义树工具对象，帮助进行指定的对象分析<br>
   * 请注意，如果其它线程也正在使用这个对象，那么本线程执行到这里将被锁定。
   * @return
   */
  public static ASTParser get() { 
    rol.lock();
    return parser;
  }
  
  /**
   * 释放当前线程已经锁定的ast抽象语义树工具对象。
   * @throws IllegalMonitorStateException - if the current thread does not hold this lock
   */
  public static void release() {
    rol.unlock();
  }
}