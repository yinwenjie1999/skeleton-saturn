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
   * 根工程的完整工程路径
   * 例如根工程的完整路径为：/usr/local/rootproject；那么这里的工程home project为：/usr/local/
   */
  private String rootHomePath;
  /**
   * 根工程的完整工程名，不是说完整的路径名
   */
  private String rootProject;
  /**
   * 实体定义工程的名字，只会有一个
   */
  private String entityProjectName;
  /**
   * 业务性质工程的名字，可能有多个
   */
  private String[] projectNames;
  
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
}