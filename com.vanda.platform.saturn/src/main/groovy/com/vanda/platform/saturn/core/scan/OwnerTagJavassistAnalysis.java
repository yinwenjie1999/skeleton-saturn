package com.vanda.platform.saturn.core.scan;

import com.vanda.platform.saturn.core.model.PersistentProperty;
import com.vanda.platform.saturn.core.model.PersistentRelation;

import javassist.CtField;

/**
 * TODO 该类的设计和目标已经明确，代码还没有写
 * @author yinwenjie
 */
public class OwnerTagJavassistAnalysis extends JavassistAnalysis {

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.JavassistAnalysis#analysisGeneralField(javassist.CtField, int)
   */
  @Override
  protected PersistentProperty analysisGeneralField(CtField fieldItem, int fieldIndex) {
    // TODO Auto-generated method stub
    return null;
  }

  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.JavassistAnalysis#analysisRelationField(java.lang.Class, javassist.CtField, int)
   */
  @Override
  protected PersistentRelation analysisRelationField(Class<?> reflectClass, CtField fieldItem, int fieldIndex) {
    // TODO Auto-generated method stub
    return null;
  }

}
