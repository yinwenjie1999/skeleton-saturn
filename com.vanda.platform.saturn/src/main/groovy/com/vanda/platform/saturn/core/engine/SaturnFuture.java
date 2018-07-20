package com.vanda.platform.saturn.core.engine;

import com.vanda.platform.saturn.core.context.SaturnContext;

/**
 * 骨架V3.0执行引擎观察器<br>
 * 通过这个观察器定义，操作者可以从主线程中（或者子线程中）方便的监控骨架V3.0执行引擎的实时执行情况
 * @author yinwenjie
 */
public interface SaturnFuture {
  /**
   * 该方法用于获取到当前执行引擎从内部完成初始化的上下文对象
   * @return
   */
  public SaturnContext getSaturnContext();
  
  /**
   * 执行引擎是否已完成初始化
   * @return 执行引擎是否已完成初始化
   */
  public Boolean isInitiated();
  
  /**
   * 执行引擎是否在执行过程中遇到了错误
   * @return
   */
  public Boolean isException();
  
  /**
   * 如果执行引擎在执行过程中遇到了错误，则调用这个方法将会返回异常错误的具体信息
   * @return
   */
  public String errorMgs();
  
  /**
   * 调用该方法，当前SaturnFuture对象所在的线程将会进行锁定状态，直到骨架V3.0执行引擎的所有代码生成过程全部结束<br>
   * ——无论执行过程是正常结束还是异常结束
   */
  public void waitForFinished();
}