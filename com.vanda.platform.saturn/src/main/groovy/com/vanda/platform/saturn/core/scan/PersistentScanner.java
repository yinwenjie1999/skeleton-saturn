package com.vanda.platform.saturn.core.scan;

import java.util.List;

import com.vanda.platform.saturn.core.engine.handler.SaturnHandler;
import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * 持久层扫描接口，该接口定义了骨架生成工具对指定工程持久层的扫描操作
 * @author yinwenjie
 */
public interface PersistentScanner extends SaturnHandler {
  /**
   * 该方法定义了骨架生成工具对目标持久层对象的扫描操作
   * @param rootHomePath 根工程的完整工程路径,例如根工程的完整路径为：/usr/local/rootproject；那么这里的工程home project为：/usr/local/
   * @param rootProject 根工程的完整工程名，不是说完整的路径名
   * @param entityProjectName 实体定义工程的名字，只会有一个
   * @return
   */
  List<PersistentClass> scan(String rootHomePath , String rootProject , String entityProjectName);
}