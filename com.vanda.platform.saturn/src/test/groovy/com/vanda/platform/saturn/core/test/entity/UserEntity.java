package com.vanda.platform.saturn.core.test.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;

import com.vanda.platform.saturn.core.engine.annotation.BuildCustomRepository;
import com.vanda.platform.saturn.core.engine.annotation.BuildUpdateMethods;
import com.vanda.platform.saturn.core.engine.annotation.BuildQueryMethods;
import com.vanda.platform.saturn.core.engine.annotation.SaturnColumn;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnUpdateMethod;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.OrderType;
import com.vanda.platform.saturn.core.engine.annotation.SaturnQueryMethod.QueryType;

/**
 * 该实体记录一键报警系统中所有需要使用鉴权登录的用户信息。这些用户包括了后台管理员用户、接警端平台用户、<br>
 * 使用派警接收端的警员用户。通过不同的角色信息，对这些用户进行区别
 * @author yinwenjie
 */
@Entity
@Table(name = "b_user")
@BuildCustomRepository
@BuildQueryMethods(methods={
  @SaturnQueryMethod(params={"account","useStatus"} , description="按照用户登录名和用户状态进行用户基本信息查询" , queryType=QueryType.EQUAL),
  @SaturnQueryMethod(params={"useStatus"} , description="按照用户状态，查询符合状态的所有信息" , queryType=QueryType.EQUAL , orderByParams={"createTime"} , orderType={OrderType.DESC})
})
@BuildUpdateMethods(methods={
  @SaturnUpdateMethod(queryParams={"account" , "useStatus"} , updateParams={"id"}, description="更新指定用户的用户状态")
})
public class UserEntity extends UuidEntity {
  /**
   * 
   */
  private static final long serialVersionUID = 318381684938863952L;
  
  /**
   * 人员姓名
   */
  @Column(name = "user_name", length = 64, nullable = false)
  @SaturnColumn(description="人员姓名" , insertable=true , nullable=false , updatable=true)
  private String userName;

  /**
   * 人员 头像
   */
  @Column(name = "user_head", length = 64, nullable = false)
  @SaturnColumn(description="人员头像" , insertable=true , updatable=true)
  private String userHead = "";

  /** 性别.0保密，1男 2女 */
  @Column(name = "gender")
  @SaturnColumn(description="性别")
  private Integer gender = 0;

  /**
   * 用户账号登录信息
   */
  @Column(name = "user_account", length = 64, nullable = false, unique = true)
  @SaturnColumn(description="用户账户")
  private String account;

  /**
   * 用户账号密码信息（经过加密的）
   */
  @Column(name = "user_password", length = 64, nullable = false)
  @SaturnColumn(description="密码")
  private String password;

  /**
   * 用户账号状态 这个属性设定好像有问题<br>
   * 1：表示可用
   * 0和其它值：表示不可用
   */
  @Column(name = "useStatus", nullable = false)
  @SaturnColumn(description="用户状态")
  private Integer useStatus = 1;

  /** 角色和人员相关的. **/
  @ManyToMany(fetch = FetchType.LAZY, mappedBy = "users")
  @SaturnColumn(description="关联角色")
  private Set<RoleEntity> roles;

  /**
   * 创建时间
   */
  @Column(name = "createTime", length = 64, nullable = false)
  @SaturnColumn(description="创建时间")
  private Date createTime=new Date();
  
  /**
   * 人员主要联系电话
   */
  @Column(name = "user_phone", length = 64, nullable = true, unique = true)
  @SaturnColumn(description="联系电话")
  private String phone = "";
  
  /**
   * 排序信息，值越大，排序越靠后
   */
  @Column(name = "listsort", nullable = false)
  @SaturnColumn(description="排序依据")
  private Integer listsort = 100;

  public String getUserName() {
    return userName;
  }

  public void setUserName(String userName) {
    this.userName = userName;
  }

  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Date getCreateTime() {
    return createTime;
  }

  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
  
  public String getUserHead() {
    return userHead;
  }

  public void setUserHead(String userHead) {
    this.userHead = userHead;
  }

  public Integer getGender() {
    return gender;
  }

  public void setGender(Integer gender) {
    this.gender = gender;
  }
  
  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public Integer getListsort() {
    return listsort;
  }

  public void setListsort(Integer listsort) {
    this.listsort = listsort;
  }
  
  public Integer getUseStatus() {
    return useStatus;
  }

  public void setUseStatus(Integer useStatus) {
    this.useStatus = useStatus;
  }

  public Set<RoleEntity> getRoles() {
    return roles;
  }

  public void setRoles(Set<RoleEntity> roles) {
    this.roles = roles;
  }
}