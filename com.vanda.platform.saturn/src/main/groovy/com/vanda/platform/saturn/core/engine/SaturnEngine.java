package com.vanda.platform.saturn.core.engine;

import java.io.File;
import java.util.List;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.context.repository.RepositoryBuilder;
import com.vanda.platform.saturn.core.engine.handler.SaturnHandler;
import com.vanda.platform.saturn.core.engine.handler.SaturnHandlerEventListener;

/**
 * 骨架V3.0（土星）执行驱动，该类就是外层调用者最近本的使用类<br>
 * 所有骨架执行前的初始化过程、规则属性设置过程，都在这里进行；工程的生成/重新生成命令也是由类调用和触发
 * @author yinwenjie
 */
public final class SaturnEngine {
  
  /**
   * TODO 注释未写
   */
  private static SaturnEngine saturnEngine;
  
  private SaturnEngine() {
    
  }
  
  /**
   * 主构建方法，这个方法中骨架组件将根据设置的各种参数信息进行初始化和工程生成/调整工作。<br>
   * 主要包括生成持久层业务对象、扫描和存储持久层对象结构、生成数据层、生成服务层、生成用户接口层等工作。
   * 
   * @return
   */
  public SaturnFuture eve() {
    /*
     * TODO 未开始编码
     * */
    return null;
  }
  
  /**
   * TODO 注释未写
   * @author Administrator
   */
  public class Builder {
    /**
     * TODO 注释未写
     */
    private RepositoryBuilder repositoryBuilder;
    
    /**
     * TODO 注释未写
     */
    private File projectRoot;
    
    /**
     * TODO 注释未写
     */
    private String entityProjectName;
    
    /**
     * TODO 注释未写
     */
    private String projectNames[];
    
    /**
     * TODO 注释未写
     */
    private List<SaturnHandler> handlers;
    
    /**
     * TODO 注释未写
     */
    private SaturnHandlerEventListener handlerEventListener;
    
    /**
     * 该方法为Saturn设置存储层的构建器，存储层构建器都实现了RepositoryBuilder接口，默认的实现为SimpleRepositoryBuilder<br>
     * 注意，所谓“Saturn的存储层构建器”，并不是说目标工程的存储层相关设置，而是说当Saturn完成了相关数据实体层结构扫描后，要有一个地方存储扫描得到的相关结构，
     * 而Saturn（土星）提供了多种存储扫描后结构的方式；默认的一种是直接存储在内存中的简单结构。
     * @param repositoryBuilder 存储层构建接口
     */
    public Builder setRepositoryBuilder(RepositoryBuilder repositoryBuilder) {
      /*
       * TODO 代码未写
       * */
      return this;
    }
    
    /**
     * 设置该属性，指定骨架组件当前操作的工程的根路径，注意必须是一个完整路径。
     * @param projectRoot 指定的完成根路径
     */
    public Builder setProjectRoot(File projectRoot) {
      /*
       * TODO 代码未写
       * */
      return this;
    }
    
    /**
     * 设置该属性，指定骨架组件当前操作的工程中（工程的根路径下），拥有持久层模型定义工程名
     * @param entityProjectName 持久层模型定义的工程名
     */
    public Builder setEntityProjectName(String entityProjectName) {
      /*
       * TODO 代码未写
       * */
      return this;
    }
    
    /**
     * 设置该属性，指定骨架组件当前正操操作的根工程中（工程的根路径下），需要进行工程骨架生成的业务层工程。
     * @param projectNames 可以指定一个或者多个完整工程名信息
     */
    public Builder setProjectNames(String ...projectNames) {
      /*
       * TODO 代码未写
       * */
      return this;
    }
    
    /**
     * TODO 注释未写
     * @param handlerEventListener
     * @return
     */
    public Builder setHandlerEventListener(SaturnHandlerEventListener handlerEventListener) {
      /*
       * TODO 代码未写
       * */
      return this;
    }
    
    /**
     * 骨架V3.0（土星）组件主要的构建方法，在这个方法将主要完成：<br>
     * 对象结构存储层构建器的初始化、数据层构建器的初始化、服务层构建器的初始化、用户层构建器的初始化等初始化工作
     * @return
     */
    public SaturnContext build() {
      /*
       * 创建过程主要有：
       * 1、监测各种外部设置是否合法、完备
       * 2、指定的List<SaturnProjectHandler>将会被排序——凡是EntityGenerateHandler抽象类的子类会排在最前面被优先处理
       *    注意：这里要重点检查PersistentScanner接口类涉及到的处理器是否已经指定，如果没有指定则处理过程会自动指定
       *    PersistentScanner接口涉及到的处理器将会在所有EntityGenerateHandler处理器运行完毕后，自动运行
       * 2、首先生成整个Saturn（土星）V3.0组件在工作中唯一的上下文对象（只有这么一个SaturnContext类的实例）
       * 3、然后生成Saturn（土星）V3.0，存储层工作接口（参见RepositoryBuilder以及其实现类）
       * 3、接着List<SaturnProjectHandler>集合中的处理器将会依次运行，且在PersistentScanner
       * TODO 继续写
       * */
      return null;
    }
    
    /**
     * @param handler
     * @return
     */
    public Builder addSaturnProjectHandler(SaturnHandler handler) {
      return null;
    }
  }
}