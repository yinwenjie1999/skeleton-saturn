package com.vanda.platform.saturn.core.engine.handler;

import com.vanda.platform.saturn.core.context.SaturnContext;

/**
 * 骨架（土星）V3.0的驱动生成接口，利用责任链模式的思路对目标工程中各层的生成工作进行“脱藕”（解决灵活性问题），且又保证了生成工作的顺序（解决规范性问题）。<br>
 * 举个例子，在骨架V3.0版本设计之初，只针对了拥有entity层、repository层、service层和controller层的典型的“缺血结构”工程，
 * 也就是说最初的最长责任链也只有entityHandler、repositoryHandler、serviceHandler和controllerHandler这四个驱动节点，对应以上例子中的四个层。<br><br>
 * 而在后续骨架（土星）V3.0版本中，将渐渐引入对DDD类型工程的支持，那么将会引入更多新的Handler驱动节点，来生成DDD类型工程中不同的“层次结构”
 * @author yinwenjie
 */
public interface SaturnHandler {
  /**
   * TODO 未写注释
   * @param context
   */
  public void handle(SaturnContext context);
}