package com.vanda.platform.saturn.core.test.entity;

import java.io.Serializable;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

/**
 * 各种实体层对象的定义中，只要包含持久层内部唯一编号的实例，都要集成该类.
 * @author yinwenjie
 */
@MappedSuperclass
public abstract class UuidEntity implements Serializable {
  /**
   * 
   */
  private static final long serialVersionUID = 6357586662390821565L;
  /**
   * 抽象实体层模型（MySQL主键）的编号信息.
   */
  @Id
  private String id;
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }
  
}
