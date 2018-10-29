/**
 * 权限.
 */
package com.vanda.platform.saturn.core.test.entity;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.vanda.platform.saturn.core.engine.annotation.SaturnColumn;
/**
 * 功能信息，用于将权限控制到功能（按钮、连接）级别
 * @author yinwenjie
 * @version V2.0
 */
@Entity
@Table(name = "t_competence")
public class CompetenceEntity extends UuidEntity {

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -7742962048681654604L;
  
  /** 状态 1正常, 0禁用（枚举）. **/
  @Type(type = "useStatus")
  @Column(name = "useStatus", nullable = false)
  @SaturnColumn(description="功能状态" , insertable=true , nullable=false , updatable=false)
  private Integer status = 1;

  /**
   * 权限对应的角色信息
   */
  @ManyToMany(mappedBy = "competences")
  private Set<RoleEntity> roles;

  /** 备注. **/
  @Column(name = "comment", nullable = false)
  private String comment = "";

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }
  
  public Set<RoleEntity> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleEntity> roles) {
    this.roles = roles;
  }
  
  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }
}