package com.vanda.platform.saturn.core.scan;

import java.util.List;

import com.vanda.platform.saturn.core.engine.handler.SaturnHandler;
import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * 持久层扫描接口，该接口定义了骨架生成工具对指定工程持久层的扫描操作（针对能够编译成class的可用工程）
 * @author yinwenjie
 */
public interface ClassScanner extends SaturnHandler {
  /**
   * 该方法定义了骨架生成工具对目标持久层对象的扫描操作——针对已编译好的class文件
   * @param projectRootPackage 需要进行class扫描的工程根包名，实际上更多的情况这个projectRootPackage就是开发工程的根包名
   * @return 
   */
  List<PersistentClass> scan(String projectRootPackage);
}
