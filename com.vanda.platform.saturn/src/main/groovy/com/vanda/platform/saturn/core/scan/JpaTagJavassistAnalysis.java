package com.vanda.platform.saturn.core.scan;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.engine.annotation.SaturnColumn;
import com.vanda.platform.saturn.core.engine.annotation.SaturnColumnRelation;
import com.vanda.platform.saturn.core.engine.annotation.SaturnEntity;
import com.vanda.platform.saturn.core.engine.annotation.SaturnValidate;
import com.vanda.platform.saturn.core.model.PersistentProperty;
import com.vanda.platform.saturn.core.model.PersistentRelation;
import com.vanda.platform.saturn.core.model.PersistentRelation.RelationType;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtPrimitiveType;
import javassist.NotFoundException;

/**
 * 基于Javassist分析技术的，主要以JPA标签描述为依据的分析过程实现
 * @author yinwenjie
 */
public class JpaTagJavassistAnalysis extends JavassistAnalysis {
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(JpaTagJavassistAnalysis.class);
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.JavassistAnalysis#analysisGeneralField(javassist.CtField, int)
   */
  @Override
  protected PersistentProperty analysisGeneralField(CtField fieldItem, int fieldIndex) {
    // 在分析一般属性时，只有具有SaturnColumn注解或者Column注解的属性才有分析的意义
    boolean hasSaturnColumnAnnotation = fieldItem.hasAnnotation(SaturnColumn.class);
    boolean hasColumnAnnotation = fieldItem.hasAnnotation(Column.class);
    boolean hasPrimaryKeyAnnotation = fieldItem.hasAnnotation(Id.class);
    if(!hasSaturnColumnAnnotation && !hasColumnAnnotation && !hasPrimaryKeyAnnotation) {
      return null;
    } 
    
    CtClass fieldType = null;
    // 基本类型只有int long byte short float double
    // java.lang.*、java.util.Date、java.math.*
    String fieldTypeName = null;
    try {
      fieldType = fieldItem.getType();
      // 如果条件成立，说明是一个原始类型
      if(fieldType instanceof CtPrimitiveType) {
        CtPrimitiveType primitiveType = (CtPrimitiveType)fieldType;
        fieldTypeName = primitiveType.getWrapperName();
      } else {
        fieldTypeName = fieldType.getName();
      }
    } catch (NotFoundException e) {
      LOGGER.debug(e.getMessage());
      return null;
    }
    
    // 如果条件成立，说明不是基础类型，不需要在这里进行处理
    if(!StringUtils.startsWith(fieldTypeName, "java.lang.")
        && !StringUtils.startsWith(fieldTypeName, "java.math.")
        && !StringUtils.equals(fieldTypeName, "java.util.Date")) {
      return null;
    }
    
    /*
     * 注意1：如果当前class是一个jpa形式的class，那么以JPA注解的设定为主，以Saturn注解的设定为辅
     * （如果有重复的，则以Saturn相关注解覆盖之）
     * 注意2：如果当前class不是一个JPA形式的class，那么以Saturn注解为准
     * 
     * 对于普通字段的分析过程如下：
     * 1、分析可能存在的主键信息
     * 2、分析可能存在的Column信息
     * 3、分析可能存在的SaturnColumn信息
     * */
    String fieldName = fieldItem.getName();
    PersistentProperty property = new PersistentProperty();
    property.setPropertyClass(fieldTypeName);
    property.setPropertyName(fieldName);
    property.setIndex(fieldIndex);
    // 1、============如果条件成立，说明是主键字段
    if(hasPrimaryKeyAnnotation) {
      property.setPrimaryKey(true);
      property.setCanUpdate(false);
      property.setUnique(true);
      property.setPropertyDbName("id");
      property.setNullable(false);
      property.setPropertyDesc("主键");
    }
    property.setPropertyClass(fieldType.getName());
    
    // 2、============
    // 如果条件成立，则首先读取Column中的信息
    if(hasColumnAnnotation) {
      Column columnAnnotation;
      try {
        columnAnnotation = (Column)fieldItem.getAnnotation(Column.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = columnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean canUpdate = columnAnnotation.updatable();
      property.setCanUpdate(canUpdate);
      boolean nullable = columnAnnotation.nullable();
      property.setNullable(nullable);
      String propertyDbName = columnAnnotation.name();
      property.setPropertyDbName(propertyDbName);
      boolean unique = columnAnnotation.unique();
      property.setUnique(unique);
    }
    
    // 3、============
    // 如果条件成立说明有SaturnColumn，这里面的属性有优先权
    // 实际上这里的一些属性，只有当prsistentClass是一个持久层模型定义时才能使用
    // TODO 但这里先不管，先记录设置情况
    if(hasSaturnColumnAnnotation) {
      SaturnColumn saturnColumnAnnotation;
      try {
        saturnColumnAnnotation = (SaturnColumn)fieldItem.getAnnotation(SaturnColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = saturnColumnAnnotation.insertable();
      property.setCanInsert(insertable);
      boolean nullable = saturnColumnAnnotation.nullable();
      property.setNullable(nullable);
      boolean pkColumn = saturnColumnAnnotation.pkColumn();
      property.setPrimaryKey(pkColumn);
      boolean unique = saturnColumnAnnotation.unique();
      property.setUnique(unique);
      boolean updatable = saturnColumnAnnotation.updatable();
      property.setCanUpdate(updatable);
      String description = saturnColumnAnnotation.description();
      property.setPropertyDesc(description);
    }
    
    // 可能的Validate信息
    boolean hasSaturnValidateAnnotation = fieldItem.hasAnnotation(SaturnValidate.class);
    if(hasSaturnValidateAnnotation) {
      SaturnValidate saturnValidateAnnotation;
      try {
        saturnValidateAnnotation = (SaturnValidate)fieldItem.getAnnotation(SaturnValidate.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      property.setValidateType(saturnValidateAnnotation.type());
    }
    
    // 分析后的字段，无论是不是持久层的模型对象，都必须使用SaturnColumn注解完成字段名称的描述
    Validate.notBlank(property.getPropertyDesc() , "必须使用SaturnColumn注解完成字段名称的描述[" + fieldItem.getName() + ":" + property.getPropertyClass() + "]");
    return property;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.JavassistAnalysis#analysisRelationField(java.lang.Class, javassist.CtField, int)
   */
  @Override
  protected PersistentRelation analysisRelationField(Class<?> reflectClass, CtField fieldItem, int fieldIndex) {
    // 在分析一般属性时，只有具有SaturnColumn注解或者Column注解的属性才有分析的意义
    boolean hasJoinColumnAnnotation = fieldItem.hasAnnotation(JoinColumn.class);
    boolean hasManyToOneAnnotation = fieldItem.hasAnnotation(ManyToOne.class);
    boolean hasManyToManyAnnotation = fieldItem.hasAnnotation(ManyToMany.class);
    boolean hasOneToManyAnnotation = fieldItem.hasAnnotation(OneToMany.class);
    boolean hasOneToOneAnnotation = fieldItem.hasAnnotation(OneToOne.class);
    boolean hasSaturnRelationAnnotation = fieldItem.hasAnnotation(SaturnColumnRelation.class);
    if(!hasJoinColumnAnnotation && !hasManyToOneAnnotation
        && !hasManyToManyAnnotation && !hasOneToManyAnnotation && !hasOneToOneAnnotation
        && !hasSaturnRelationAnnotation) {
      return null;
    } 
    boolean hasSaturnColumnAnnotation = fieldItem.hasAnnotation(SaturnColumn.class);
    
    CtClass fieldType = null;
    // 关联的信息实际上
    String fieldTypeName = null;
    String fieldName = fieldItem.getName();
    try {
      fieldType = fieldItem.getType();
      // 如果条件成立，说明是一个原始类型，那么就不是关联类型了
      if(fieldType instanceof CtPrimitiveType) {
        return null;
      } else {
        fieldTypeName = fieldType.getName();
      }
    } catch (NotFoundException e) {
      LOGGER.debug(e.getMessage());
      return null;
    }
    
    // 如果条件成立，说明不是基础类型，不需要在这里进行处理
    if(StringUtils.startsWith(fieldTypeName, "java.lang.")
        || StringUtils.startsWith(fieldTypeName, "java.math.")
        || StringUtils.equals(fieldTypeName, "java.util.Date")) {
      return null;
    }
    
    // 如果当前类型是一个单泛型集合，那么取出泛型类型才是真的类型
    boolean isCollectionInterface = false;
    ClassPool classPool = ClassPool.getDefault();
    // 如果以下代码判定成功，说明当前字段是一个单泛型集合性质的类
    try {
      CtClass collectionClass = classPool.get("java.util.Collection");
      isCollectionInterface = fieldType.subtypeOf(collectionClass);
      if(isCollectionInterface) {
        // 如果以代码段判定成功，则说明这是一个集合泛型，要以集合中的泛型类型作为字段的类型
        // 并且还需要验证这个泛型类型是否为一个合法的模型定义：既是有Entity或者SaturnEntity
        Field reflectField = reflectClass.getDeclaredField(fieldName);
        Type genericType = reflectField.getGenericType();
        // 如果条件成立才说明在反射类型描述中存在泛型信息
        if(genericType instanceof ParameterizedType) {
          ParameterizedType pt = (ParameterizedType) genericType;  
          Class<?> genericClazz = (Class<?>)pt.getActualTypeArguments()[0];
          fieldTypeName = genericClazz.getName();
        }
      }
    } catch (NoSuchFieldException | SecurityException | NotFoundException e) {
      LOGGER.warn(e.getMessage());
      return null;
    }
    
    // 接下来进行验证这个fieldTypeName是一个符合要求的模型描述，否则也不进行处理了
    CtClass paramsClass;
    try {
      paramsClass = classPool.get(fieldTypeName);
    } catch (NotFoundException e) {
      LOGGER.error(e.getMessage() , e);
      return null;
    }
    boolean hasEntityAnnotation = paramsClass.hasAnnotation(Entity.class);
    boolean hasSaturnEntityAnnotation = paramsClass.hasAnnotation(SaturnEntity.class);
    if(!hasEntityAnnotation && !hasSaturnEntityAnnotation) {
      return null;
    }
    
    // 如果存在JoinColumn，则首先根据其中属性设置属性特性
    PersistentRelation relation = new PersistentRelation();
    relation.setPropertyClass(fieldTypeName);
    relation.setPropertyName(fieldName);
    relation.setIndex(fieldIndex);
    if(hasJoinColumnAnnotation) {
      JoinColumn columnAnnotation;
      try {
        columnAnnotation = (JoinColumn)fieldItem.getAnnotation(JoinColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = columnAnnotation.insertable();
      relation.setCanInsert(insertable);
      boolean canUpdate = columnAnnotation.updatable();
      relation.setCanUpdate(canUpdate);
      boolean nullable = columnAnnotation.nullable();
      relation.setNullable(nullable);
      String propertyDbName = columnAnnotation.name();
      relation.setPropertyDbName(propertyDbName);
    }
    
    // 如果条件成立说明有SaturnColumn，这里面的属性有优先权
    // 实际上这里的一些属性，只有当prsistentClass是一个持久层模型定义时才能使用
    // TODO 但这里先不管，先记录设置情况
    if(hasSaturnColumnAnnotation) {
      SaturnColumn saturnColumnAnnotation;
      try {
        saturnColumnAnnotation = (SaturnColumn)fieldItem.getAnnotation(SaturnColumn.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage());
        return null;
      }
      boolean insertable = saturnColumnAnnotation.insertable();
      relation.setCanInsert(insertable);
      boolean nullable = saturnColumnAnnotation.nullable();
      relation.setNullable(nullable);
      boolean updatable = saturnColumnAnnotation.updatable();
      relation.setCanUpdate(updatable);
      String description = saturnColumnAnnotation.description();
      relation.setPropertyDesc(description);
    }
    
    // 建立关联
    if(hasManyToOneAnnotation) {
      relation.setRelationType(RelationType.ManyToOne);
    } else if(hasManyToManyAnnotation) {
      relation.setRelationType(RelationType.ManyToMany);
    } else if(hasOneToManyAnnotation) {
      relation.setRelationType(RelationType.OneToMany);
    } else if(hasOneToOneAnnotation) {
      relation.setRelationType(RelationType.OneToOne);
    } else if(hasSaturnRelationAnnotation) {
      SaturnColumnRelation saturnRelationAnnotation;
      try {
        saturnRelationAnnotation = (SaturnColumnRelation)fieldItem.getAnnotation(SaturnColumnRelation.class);
      } catch (ClassNotFoundException e) {
        LOGGER.warn(e.getMessage() + "：错误的关联类型，请检查建模情况");
        return null;
      }
      SaturnColumnRelation.RelationType pRelationType = saturnRelationAnnotation.type();
      if(pRelationType == SaturnColumnRelation.RelationType.MANYTOMANY) {
        relation.setRelationType(RelationType.ManyToMany);
      } else if(pRelationType == SaturnColumnRelation.RelationType.MANYTOONE) {
        relation.setRelationType(RelationType.ManyToOne);
      } else if(pRelationType == SaturnColumnRelation.RelationType.ONETOMANY) {
        relation.setRelationType(RelationType.OneToMany);
      } else if(pRelationType == SaturnColumnRelation.RelationType.ONETOONE) {
        relation.setRelationType(RelationType.OneToOne);
      }
    } else {
      LOGGER.warn("错误的关联类型，请检查建模情况");
      return null;
    }
    
    Validate.notBlank(relation.getPropertyDesc() , "必须使用SaturnColumn注解完成字段名称的描述[" + fieldItem.getName() + ":" + relation.getPropertyClass() + "]");
    return relation;
  }
}