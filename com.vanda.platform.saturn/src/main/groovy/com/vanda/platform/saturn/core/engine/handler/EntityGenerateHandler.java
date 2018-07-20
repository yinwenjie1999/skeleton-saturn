package com.vanda.platform.saturn.core.engine.handler;

import java.net.URL;

import com.vanda.platform.saturn.core.context.SaturnContext;

/**
 * 基于骨架（土星）V3.0的驱动生成接口的“实体定义层”生成抽象类，通过实现这个抽象类，不同的生成组件将可以生成不同的实体定义层代码。
 * 例如，当目标工程引入了“验证码”模块后，工程的实体层将会自动生成“验证码和手机号对应情况信息”的实体层（记为A），<br>
 * 那么A这个实体的生成将通过对这个EntityGenerateHandler抽象类的实现来进行。<br>
 * A的定义文件将通过freemark组件进行定义，详细情况请参见技术文档中关于类模板定义部分的描述。
 * @author yinwenjie
 */
public abstract class EntityGenerateHandler implements SaturnHandler {
  
  /**
   * 已指定的实体层文件信息
   */
  protected URL entityUrls[];
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.SaturnProjectHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  public void handle(SaturnContext context) {
    // TODO 在该方法中调用获得setEntityFilePath中设置的文件位置，并开始在指定位置生成实体类
  }
  
  /**
   * 设定当前实体定义文件的位置，可以是当前工程中某个classpath的位置，也可以是工程所在操作系统磁盘上的某一个位置。
   * @param entityUrls 指定的实体文件所在的位置
   */
  public abstract void setEntityFilePath(URL ... entityUrls);
}