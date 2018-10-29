package com.vanda.platform.saturn.core.scan;

import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import com.vanda.platform.saturn.core.model.PersistentClass;

/**
 * JDT分析接口，目前已知的分析套件包括了JPA系列标签和自有系列标签
 * @author yinwenjie
 */
public interface JDTAnalysis {
  /**
   * 进行语义分析，得到实体定义模型的结构化表达
   * @param publicTypeDecl 
   * @param currentAnnotations 
   * @param imports 
   * @return 
   */
  public PersistentClass analyze(TypeDeclaration publicTypeDecl , List<Annotation> currentAnnotations , List<ImportDeclaration> imports);
}