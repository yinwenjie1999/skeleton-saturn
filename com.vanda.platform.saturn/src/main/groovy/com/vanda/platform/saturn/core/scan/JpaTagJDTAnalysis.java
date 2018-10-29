package com.vanda.platform.saturn.core.scan;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * TODO 注释未写
 * @author yinwenjie
 */
public class JpaTagJDTAnalysis implements JDTAnalysis {
  @Override
  public PersistentClass analyze(TypeDeclaration publicTypeDecl , List<Annotation> currentAnnotations , List<ImportDeclaration> imports) {
    /*
     * 基于自有标签的相关扫描过程如下：
     * 一、确定类定义上的各种注解描述信息，包括：
     * 1、必须存在的@Table标记，和其中的name属性
     * 2、必须存在的@TableDesc标记，和其中唯一的value属性
     * 3、可能存在的@BuildCustomRepository标记
     * 4、可能存在的@BuildQueryMethods标记和其子标记（属性）@QueryMethod
     * 5、可能存在的@BuildIndependentUpdateMethods标记和其子标记@IndependentUpdateMethod
     * 
     * 二、确定类中各个private属性上的注解描述信息，包括：
     * 1、可能存在的@Column标记以及重要属性，注意：其中的insertable属性和updatable属性。
     * 这两个属性在Column标记中默认为true；但为了保证生成的业务代码的严谨性，这两个属性在骨架组件中，默认为false
     * 2、可能存在的@ManyToOne标记、@ManyToMany标记、@OneToMany标记
     * 3、必须存在的@ColumnDesc标记
     * 4、可能存在的@Validate标记
     * 
     * 三、确定类中的个get、set方法（因为对于信息的描述可能存在于方法上）
     * 1、可能存在的@Column标记以及重要属性，注意：其中的insertable属性和updatable属性。
     * 这两个属性在Column标记中默认为true；但为了保证生成的业务代码的严谨性，这两个属性在骨架组件中，默认为false
     * 2、可能存在的@ManyToOne标记、@ManyToMany标记、@OneToMany标记
     * 3、必须存在的@ColumnDesc标记
     * 4、可能存在的@Validate标记
     * 
     * 四、去重处理后，最终生成PersistentClass对象
     * */
    
    PersistentClass persistentClass = new PersistentClass();
    FieldDeclaration[] Fields = publicTypeDecl.getFields();
    // TODO 继续写
    
    // TODO Auto-generated method stub
    return null;
  }
}