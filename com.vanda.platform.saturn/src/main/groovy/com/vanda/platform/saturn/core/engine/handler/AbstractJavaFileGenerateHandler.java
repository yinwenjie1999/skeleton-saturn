package com.vanda.platform.saturn.core.engine.handler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 这个抽象处理器封装了帮助子级Handle完成java文件处理的公共使用的工具性质方法。<br>
 * 目前来看HibernateJpaRepositoryGenerateHandler、IbatisRepositoryGenerateHandler、SimpleControllerGenerateHandler
 * 等Java文件操作性质的处理器都可能会使用这些工具性质方法
 * @author yinwenjie
 *
 */
abstract class AbstractJavaFileGenerateHandler {
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJavaFileGenerateHandler.class);
  
  /**
   * 一般java原文件内容创建完成，就应该在磁盘上创建这个文件，或者这多个文件<br>
   * 该私有方法用于完成指定java文件，基于指定文件内容，在正确磁盘位置上的构建
   */
  protected void buildJavaFile(String fileContexts , String projectSrcAbsolutePath , String packageName , String simpleClassName) {
    if(StringUtils.isBlank(fileContexts) || StringUtils.isBlank(projectSrcAbsolutePath)
        || StringUtils.isBlank(packageName) || StringUtils.isBlank(simpleClassName)) {
      LOGGER.warn("错误的文件内容或者地址格式，该文件创建过程被忽略，请检查!!");
      return;
    }
    
    String packagePath = StringUtils.replace(packageName, ".", "/");
    String javaDirAbsolutePath = StringUtils.join(projectSrcAbsolutePath , "/" , packagePath);
    String javaFileName = StringUtils.join(simpleClassName , ".java");
    String javaFileAbsolutePath = StringUtils.join(javaDirAbsolutePath , "/" , javaFileName);
    File currentDir = new File(javaDirAbsolutePath);
    currentDir.mkdirs();
    // TODO 由于目前表单引擎并不支持文件对比与合并，所以这里暂时的策略时，如果文件存在就不再进行添加
    File javaFile = new File(javaFileAbsolutePath);
    if(javaFile.exists()) {
      LOGGER.warn(simpleClassName + "文件已经存在，本次创建过程被忽略!!");
      return;
    } else {
      try {
        javaFile.createNewFile();
      } catch (IOException e) {
        LOGGER.error(e.getMessage() , e);
        return;
      }
    }
    
    // 开始写入文件内容
    try (InputStream in = new ByteArrayInputStream(fileContexts.getBytes());
        OutputStream out = new FileOutputStream(javaFile)) {
      int maxlen = 9068;
      byte[] lenBytes = new byte[maxlen];
      int realLen;
      while((realLen = in.read(lenBytes, 0, maxlen)) != -1) {
        out.write(lenBytes, 0, realLen);
      }
    } catch(IOException e) {
      LOGGER.error(e.getMessage() , e);
      return;
    }
  }
}
