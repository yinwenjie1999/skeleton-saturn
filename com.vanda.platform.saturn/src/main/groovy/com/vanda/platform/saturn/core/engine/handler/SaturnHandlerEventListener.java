package com.vanda.platform.saturn.core.engine.handler;

import com.vanda.platform.saturn.core.context.SaturnContext;

/**
 * 骨架V3.0驱动引擎，事件监听器<br>
 * 事件监听器的作用是，结合骨架V3.0内部的上下文对象，在某一个处理器开始或者完成处理之前，提供人为外部干预的可能性
 * @author yinwenjie
 */
public interface SaturnHandlerEventListener {
  /**
   * TODO 这里的参数还需要进行调整
   * TODO 注释未写
   * @param e
   */
  public void onHandlerException(Throwable e);
  
  /**
   * TODO 注释未写
   * @param context
   * @param handler
   */
  public void onHandlerInitiated(SaturnContext context , SaturnHandler handler);
  
  /**
   * TODO 注释未写
   * @param context
   * @param handler
   */
  public void onHandlerExecuted(SaturnContext context , SaturnHandler handler);
}