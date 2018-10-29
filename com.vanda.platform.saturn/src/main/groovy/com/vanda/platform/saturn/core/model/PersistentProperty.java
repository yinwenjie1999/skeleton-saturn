package com.vanda.platform.saturn.core.model;

import com.vanda.platform.saturn.core.engine.annotation.SaturnValidate;

/**
 * 持久化属性
 * @author yinwenjie
 */
public class PersistentProperty {
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
   * 属性类型（完整类型），无论属性是否是java中的类型
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
  /**
   * 该属性是否必须唯一
   */
  private Boolean unique = false;
  /**
   * 是否是主键
   */
  private Boolean primaryKey = false;
  
  /**
   * 该字段如果设定了验证信息，则验证信息在这里被记录
   */
  private SaturnValidate.ValidateType validateType = null;
  
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
  public Boolean getUnique() {
    return unique;
  }
  public void setUnique(Boolean unique) {
    this.unique = unique;
  }
  public Boolean getPrimaryKey() {
    return primaryKey;
  }
  public void setPrimaryKey(Boolean primaryKey) {
    this.primaryKey = primaryKey;
  }
  public SaturnValidate.ValidateType getValidateType() {
    return validateType;
  }
  public void setValidateType(SaturnValidate.ValidateType validateType) {
    this.validateType = validateType;
  }
  public Boolean getCanInsert() {
    return canInsert;
  }
  public void setCanInsert(Boolean canInsert) {
    this.canInsert = canInsert;
  }
}