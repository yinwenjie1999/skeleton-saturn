package com.vanda.platform.saturn.core.engine.handler;

import com.vanda.platform.saturn.core.context.SaturnContext;

/**
 * 该处理驱动负责根据完成的实体层扫描内容，按照设置的要求生成存储层代码
 * @author yinwenjie
 */
public abstract class RepositoryGenerateHandler implements SaturnHandler {
  
  protected String domains[];
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.SaturnProjectHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  @Override
  public void handle(SaturnContext context) {
    // TODO 在该方法中调用获得setEntityFilePath中设置的文件位置，并开始在指定位置生成存储层类
  }
  
  /**
   * 设置本生成驱动器不负责的domain域，也就是说这些指定域下存储层代码不由本驱动器负责<br>
   * 注意不能排除默认域（也就是没有设置域信息的实体模型定义）的生成。<br>
   * 如果不设置，就是默认参考所有域下的实体层定义，进行存储层的生成
   * @param domains
   */
  public void setExcludeDomain(String ...domains) {
    this.domains = domains;
  }
  
  // TODO 这里定义方法，用于设置生成规则的各种细节，TODO 还没有完成定义
//  public void setXXX
}