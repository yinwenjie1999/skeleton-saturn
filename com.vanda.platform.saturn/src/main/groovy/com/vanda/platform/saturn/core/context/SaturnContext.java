package com.vanda.platform.saturn.core.context;

import com.vanda.platform.saturn.core.context.service.PersistentClassService;
import com.vanda.platform.saturn.core.context.service.PersistentPropertyService;
import com.vanda.platform.saturn.core.context.service.PersistentQueryMethodService;
import com.vanda.platform.saturn.core.context.service.PersistentRelationService;
import com.vanda.platform.saturn.core.context.service.PersistentUpdateMethodService;

/**
 * 骨架V3.0（土星）上下文对象，是整个组建中最重要的对象，在工程持久层、数据层、服务层、用户接口层的生成过程中起到承上启下的作用。<br>
 * 通过该对象，开发人员可以了解生成过程状态，获取到上一个生成步骤的基本信息。<br>
 * 在整个组件中SaturnContext对象只有一个，并采用构建器模式完成初始化。
 * @author yinwenjie
 */
public abstract class SaturnContext {  
  /**
   * TODO 注释未写
   */
  protected PersistentClassService persistentClassService;
  /**
   * TODO 注释未写
   */
  protected PersistentPropertyService persistentPropertyService;
  /**
   * TODO 注释未写
   */
  protected PersistentQueryMethodService persistentQueryMethodService;
  /**
   * TODO 注释未写
   */
  protected PersistentRelationService persistentRelationService;
  /**
   * TODO 注释未写
   */
  protected PersistentUpdateMethodService persistentUpdateMethodService;
  /**
   * TODO 目前还未使用
   * 根工程的完整工程路径,例如根工程的完整路径为：/usr/local/rootproject；那么这里的工程home project为：/usr/local/
   */
  private String rootHomePath;
  /**TODO 目前还未使用
   * 根工程的完整工程名，不是说完整的路径名
   */
  private String rootProject;
  /**
   * TODO 目前还未使用
   * 实体定义工程的名字，只会有一个
   */
  private String entityProjectName;
  /**
   * TODO 目前还未使用
   * 业务性质工程的名字，可能有多个
   */
  private String[] projectNames;
  /**
   * 需要进行模型扫描的根路径，可以是本工程的，也可以是其它依赖工程的
   */
  private String[] rootScanPackages;
  /**
   * 当前工程的绝对路径，完成路径例如：/usr/local，或者c:/testw/project
   */
  private String projectAbsolutePath;
  
  /**
   * 当前工程的主要源代码（非测试代码）存在的相对路径，既最可能是/src/main/java的绝对路径
   */
  private String projectSrcPath;
  
  /**
   * 需要进行持久层、服务层、http层代码生成的根包位置<br>
   * 例如：xxx.yyyy.ttt，那么默认的service的位置为：xxx.yyyy.ttt.service
   */
  private String rootPackage;
  
  public PersistentClassService getPersistentClassService() {
    return persistentClassService;
  }
  public void setPersistentClassService(PersistentClassService persistentClassService) {
    this.persistentClassService = persistentClassService;
  }
  public PersistentPropertyService getPersistentPropertyService() {
    return persistentPropertyService;
  }
  public void setPersistentPropertyService(PersistentPropertyService persistentPropertyService) {
    this.persistentPropertyService = persistentPropertyService;
  }
  public PersistentQueryMethodService getPersistentQueryMethodService() {
    return persistentQueryMethodService;
  }
  public void setPersistentQueryMethodService(
      PersistentQueryMethodService persistentQueryMethodService) {
    this.persistentQueryMethodService = persistentQueryMethodService;
  }
  public PersistentRelationService getPersistentRelationService() {
    return persistentRelationService;
  }
  public void setPersistentRelationService(PersistentRelationService persistentRelationService) {
    this.persistentRelationService = persistentRelationService;
  }
  public PersistentUpdateMethodService getPersistentUpdateMethodService() {
    return persistentUpdateMethodService;
  }
  public void setPersistentUpdateMethodService(
      PersistentUpdateMethodService persistentUpdateMethodService) {
    this.persistentUpdateMethodService = persistentUpdateMethodService;
  }
  public String getRootHomePath() {
    return rootHomePath;
  }
  public void setRootHomePath(String rootHomePath) {
    this.rootHomePath = rootHomePath;
  }
  public String getRootProject() {
    return rootProject;
  }
  public void setRootProject(String rootProject) {
    this.rootProject = rootProject;
  }
  public String getEntityProjectName() {
    return entityProjectName;
  }
  public void setEntityProjectName(String entityProjectName) {
    this.entityProjectName = entityProjectName;
  }
  public String[] getProjectNames() {
    return projectNames;
  }
  public void setProjectNames(String[] projectNames) {
    this.projectNames = projectNames;
  }
  public String[] getRootScanPackages() {
    return rootScanPackages;
  }
  public void setRootScanPackages(String[] rootScanPackages) {
    this.rootScanPackages = rootScanPackages;
  }
  public String getProjectAbsolutePath() {
    return projectAbsolutePath;
  }
  public void setProjectAbsolutePath(String projectAbsolutePath) {
    this.projectAbsolutePath = projectAbsolutePath;
  }
  public String getRootPackage() {
    return rootPackage;
  }
  public void setRootPackage(String rootPackage) {
    this.rootPackage = rootPackage;
  }
  public String getProjectSrcPath() {
    return projectSrcPath;
  }
  public void setProjectSrcPath(String projectSrcPath) {
    this.projectSrcPath = projectSrcPath;
  }
}