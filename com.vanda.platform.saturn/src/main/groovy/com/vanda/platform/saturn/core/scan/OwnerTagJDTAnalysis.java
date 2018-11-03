package com.vanda.platform.saturn.core.scan;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * TODO 注释未写
 * @author yinwenjie
 */
public class OwnerTagJDTAnalysis implements JDTAnalysis {
  @Override
  public PersistentClass analyze(TypeDeclaration publicTypeDecl , List<Annotation> currentAnnotations , List<ImportDeclaration> imports) {
    // TODO Auto-generated method stub
    return null;
  }
}