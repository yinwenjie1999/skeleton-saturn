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
   * 该模型属性所属的class信息——完整的class信息
   */
  private String persistentClassName;
  /**
   * 本持久化类型中，该属性的关联类型
   * 是一个枚举，以方便建立相关关联查询
   */
  private RelationType relationType;
  /**
   * 该属性在持久化类中的排序索引。第一个属性编号为0
   */
  private Integer index;
  /**
   * 属性名
   * （英文名）
   */
  private String propertyName;
  /**
   * 该属性名的描述
   */
  private String propertyDesc;
  /**
   * 该属性名对应的数据库字段名
   */
  private String propertyDbName;
  /**
   * 属性类型（完整类型），一般来说肯定是一个其它数据模型的完整类名
   */
  private String propertyClass; 
  /**
   * 该属性在持久化类中是否可以进行基础插入
   */
  private Boolean canInsert = true;
  /**
   * 该属性在持久化类中是否可以进行基础更新
   */
  private Boolean canUpdate = false;
  /**
   * 该属性是否必须有值
   */
  private Boolean nullable = false;
  
  public RelationType getRelationType() {
    return relationType;
  }
  public void setRelationType(RelationType relationType) {
    this.relationType = relationType;
  }
  public String getPersistentClassName() {
    return persistentClassName;
  }
  public void setPersistentClassName(String persistentClassName) {
    this.persistentClassName = persistentClassName;
  }
  public Integer getIndex() {
    return index;
  }
  public void setIndex(Integer index) {
    this.index = index;
  }
  public String getPropertyName() {
    return propertyName;
  }
  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }
  public String getPropertyDesc() {
    return propertyDesc;
  }
  public void setPropertyDesc(String propertyDesc) {
    this.propertyDesc = propertyDesc;
  }
  public String getPropertyDbName() {
    return propertyDbName;
  }
  public void setPropertyDbName(String propertyDbName) {
    this.propertyDbName = propertyDbName;
  }
  public String getPropertyClass() {
    return propertyClass;
  }
  public void setPropertyClass(String propertyClass) {
    this.propertyClass = propertyClass;
  }
  public Boolean getCanInsert() {
    return canInsert;
  }
  public void setCanInsert(Boolean canInsert) {
    this.canInsert = canInsert;
  }
  public Boolean getCanUpdate() {
    return canUpdate;
  }
  public void setCanUpdate(Boolean canUpdate) {
    this.canUpdate = canUpdate;
  }
  public Boolean getNullable() {
    return nullable;
  }
  public void setNullable(Boolean nullable) {
    this.nullable = nullable;
  }
}