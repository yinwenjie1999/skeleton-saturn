package com.vanda.platform.saturn.core.context;

import com.vanda.platform.saturn.core.context.repository.DomainRepository;
import com.vanda.platform.saturn.core.context.repository.IndependentUpdateMethodRepository;
import com.vanda.platform.saturn.core.context.repository.PersistentClassRepository;
import com.vanda.platform.saturn.core.context.repository.QueryMethodRepository;

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
  protected DomainRepository domainRepository;
  /**
   * TODO 注释未写
   */
  protected IndependentUpdateMethodRepository independentUpdateMethodRepository;
  /**
   * TODO 注释未写
   */
  protected PersistentClassRepository persistentClassRepository;
  /**
   * TODO 注释未写
   */
  protected QueryMethodRepository queryMethodRepository;
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
  
  public DomainRepository getDomainRepository() {
    return domainRepository;
  }
  public void setDomainRepository(DomainRepository domainRepository) {
    this.domainRepository = domainRepository;
  }
  public IndependentUpdateMethodRepository getIndependentUpdateMethodRepository() {
    return independentUpdateMethodRepository;
  }
  public void setIndependentUpdateMethodRepository(
      IndependentUpdateMethodRepository independentUpdateMethodRepository) {
    this.independentUpdateMethodRepository = independentUpdateMethodRepository;
  }
  public PersistentClassRepository getPersistentClassRepository() {
    return persistentClassRepository;
  }
  public void setPersistentClassRepository(PersistentClassRepository persistentClassRepository) {
    this.persistentClassRepository = persistentClassRepository;
  }
  public QueryMethodRepository getQueryMethodRepository() {
    return queryMethodRepository;
  }
  public void setQueryMethodRepository(QueryMethodRepository queryMethodRepository) {
    this.queryMethodRepository = queryMethodRepository;
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