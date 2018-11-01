/**
 * 角色.
 */
package com.vanda.platform.saturn.core.test.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

/**
 * 角色信息，用于描述标准的spring security角色
 * @author yinwenjie
 * @version V2.0
 */
@Entity
@Table(name = "t_role")
public class RoleEntity extends UuidEntity {

  /**
   * serialVersionUID.
   */
  private static final long serialVersionUID = -4750396018968101826L;

  /** 角色名称. **/
  @Column(name = "name", length = 64, nullable = false, unique = true)
  private String name = "";

  /** 创建时间. **/
  @Column(name = "create_date", nullable = false)
  private Date createDate = new Date();

  /** 修改时间. **/
  @Column(name = "modify_date")
  private Date modifyDate;

  /** 状态 1正常, 0或者其它值：禁用. **/
  @Column(name = "status", nullable = false)
  private Integer status = 1;

  /** 备注.角色信息说明 **/
  @Column(name = "comment", length = 64, nullable = true)
  private String comment = "";

  /**
   * 这个角色对应的所有需要进行鉴权的用户，包括后台管理员用户、接警端平台用户、
   * 使用派警接收端的警员用户。通过不同的角色信息，对这些用户进行区别
   */
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "role_user_mapping", joinColumns = {@JoinColumn(name = "role_id")}, inverseJoinColumns = {@JoinColumn(name = "user_id")})
  private Set<UserEntity> users;

  /** 角色和功能的对应关系. **/
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(name = "role_competence_mapping", joinColumns = {@JoinColumn(name = "role_id")}, inverseJoinColumns = {@JoinColumn(name = "competence_id")})
  private Set<CompetenceEntity> competences;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Date getCreateDate() {
    return createDate;
  }

  public void setCreateDate(Date createDate) {
    this.createDate = createDate;
  }

  public Date getModifyDate() {
    return modifyDate;
  }

  public void setModifyDate(Date modifyDate) {
    this.modifyDate = modifyDate;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public Set<CompetenceEntity> getCompetences() {
    return competences;
  }

  public void setCompetences(Set<CompetenceEntity> competences) {
    this.competences = competences;
  }

  public Set<UserEntity> getUsers() {
    return users;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

  public void setUsers(Set<UserEntity> users) {
    this.users = users;
  }
}