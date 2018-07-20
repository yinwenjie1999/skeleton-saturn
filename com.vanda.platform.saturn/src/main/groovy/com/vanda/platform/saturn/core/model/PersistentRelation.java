package com.vanda.platform.saturn.core.model;

/**
 * 该信息描述一个持久化类与另一个持久化类间的关联信息
 * @author yinwenjie
 */
public class PersistentRelation {
  
  public enum RelationType {
    OneToOne, OneToMany, ManyToOne,ManyToMany
  }
  
  /**
   * 关联时所使用的属性
   */
  private PersistentProperty property;
  
  /**
   * 本持久化类型中，该属性的关联类型
   * 是一个枚举，以方便建立相关关联查询
   */
  private RelationType relationType;

  public PersistentProperty getProperty() {
    return property;
  }

  public void setProperty(PersistentProperty property) {
    this.property = property;
  }

  public RelationType getRelationType() {
    return relationType;
  }

  public void setRelationType(RelationType relationType) {
    this.relationType = relationType;
  }
}