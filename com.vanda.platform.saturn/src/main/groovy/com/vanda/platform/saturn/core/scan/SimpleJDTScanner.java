package com.vanda.platform.saturn.core.scan;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vanda.platform.saturn.core.context.SaturnContext;
import com.vanda.platform.saturn.core.model.PersistentClass;
import com.vanda.platform.saturn.core.utils.JdtUtils;

/**
 * 这是一个持久层扫描定义的默认实现，是骨架V3.0版本自带的持久层扫描逻辑
 * @author yinwenjie
 */
public class SimpleJDTScanner implements JDTScanner {
  
  /**
   * 日志
   */
  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleJDTScanner.class);
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.engine.handler.SaturnProjectHandler#handle(com.vanda.platform.saturn.core.context.SaturnContext)
   */
  @Override
  public void handle(SaturnContext context) {
    String rootHomePath = context.getRootHomePath();
    String rootProject = context.getRootProject();
    String entityProjectName = context.getEntityProjectName();
    
    // TODO 相关验证信息
    Validate.notBlank(rootHomePath , "");
    Validate.notBlank(rootProject , "");
    Validate.notBlank(entityProjectName , "");
    
    this.scan(rootHomePath, rootProject, entityProjectName);
  }
  
  /* (non-Javadoc)
   * @see com.vanda.platform.saturn.core.scan.PersistentScanner#scan(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<PersistentClass> scan(String rootHomePath, String rootProject, String entityProjectName) {
    String[] rootHomePathItems = rootHomePath.split("[\\\\|/]{1}");
    String entityProjectAbsolutePath = StringUtils.join(Arrays.asList(rootHomePathItems , rootProject , entityProjectName), System.lineSeparator());
    return this.scanHandle(new File(entityProjectAbsolutePath).listFiles());
  }
  
  /**
   * TODO 注释未写
   * @param files
   * @return
   */
  private List<PersistentClass> scanHandle(File[] files) {
    List<PersistentClass> persistentClasses = new ArrayList<>();
    try {
      for(int index = 0 ; files!= null && index < files.length ; index++) {
        File currentFile = files[index];
        if(currentFile.isDirectory()) {
          this.scanHandle(currentFile.listFiles());
        }
        
        // 如果条件成立，说明是一个java文件，则需要进行扫描（当然，是不是能够成功建立ast则是另一码事）
        String fileName = currentFile.getName();
        int nodeIndex = fileName.lastIndexOf(".");
        String indexValue = fileName.substring(nodeIndex);
        if(nodeIndex != -1 && indexValue.equalsIgnoreCase(".java")) {
          PersistentClass persistentClass = scanEntityFile(currentFile);
          if(persistentClass != null) {
            persistentClasses.add(persistentClass);
          }
        }
      }
    } catch (IllegalMonitorStateException e) {
      LOGGER.error(e.getMessage() , e);
      throw new RuntimeException(e);
    } catch(IOException es) {
      // 这种情况下只会提示警告
      LOGGER.warn(es.toString() , es);
    } finally {
      JdtUtils.release();
    }
    
    return persistentClasses;
  }
  
  /**
   * TODO 注释未写
   * @param javaFile
   * @return
   */
  @SuppressWarnings("unchecked")
  private PersistentClass scanEntityFile(File javaFile) throws IOException {
    /*
     * 对于单个java文件的扫描主要遵循以下处理原则和处理过程
     * 1、将当前java文件带入JDT工具，得到当前的JDT解析对象AST（抽象语义树），如果抛出异常则不再解析
     * 2、由于骨架支持自定义标签和原生JPA标签（以后者优先），所以只有java主类上有@javax.persistence.Entity注解或者
     * @com.vanda.platform.saturn.core.engine.annotation.SaturnEntity注解，那么才会继续解析这个类
     * 3、由于有两套标签库的支持，所以解析过程也分为两套，且以JPA标签为优先，所以根据第二步得到的关键注释，
     * 再决定是使用JpaTagAnalysis进行分析，还是使用OwnerTagAnalysis进行分析（TODO 这里的解耦设计待后续版本再进行优化）
     * 4、得到的PersistentClass对象将会被构建后返回
     * */
    ASTParser astParserTool = JdtUtils.get();
    
    // 1、===========================================
    // 开始读取java文件
    BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(javaFile));
    byte[] input = new byte[bufferedInputStream.available()];
    bufferedInputStream.read(input);
    bufferedInputStream.close();
    astParserTool.setSource(new String(input , "UTF-8").toCharArray());
    
    // 2、===========================================
    CompilationUnit javaUnit = (CompilationUnit) astParserTool.createAST(null);
    if(javaUnit == null) {
      return null;
    }
    // 获取已经导入的import信息
    List<ImportDeclaration> imports = javaUnit.imports();
    // 获取包名和完整类名
    PackageDeclaration packageDeclaration = javaUnit.getPackage();
    String packageName = packageDeclaration.getName().toString();
    TypeDeclaration publicTypeDecl = (TypeDeclaration)javaUnit.types().get(0);
    if(publicTypeDecl == null) {
      return null;
    }
    String className = publicTypeDecl.getName().toString();
    String fullClassName = StringUtils.join(packageName ,  className , ".");
    // 获取主类上的注解信息,以及类的访问修饰符标识
    List<?> modifiers = publicTypeDecl.modifiers();
    List<Annotation> currentAnnotations = new ArrayList<>();
    List<Modifier> currentModifiers = new ArrayList<>();
    for (Object item : modifiers) {
      if(item instanceof Annotation) {
        Annotation annotation = (Annotation)item;
        currentAnnotations.add(annotation);
      }
      if(item instanceof Modifier) {
        Modifier modifier = (Modifier)item;
        currentModifiers.add(modifier);
      }
    }
    
    // 3、=================================
    // TODO 异常信息需要重新定义
    // 验证当前类规格和注解规格
    int validateResult = this.validateClassType(currentAnnotations, currentModifiers, imports);
    JDTAnalysis tagAnalysis;
    switch (validateResult) {
      case 0:
        return null;
      case 1: 
        tagAnalysis = new JpaTagJDTAnalysis();
        break;
      case 2: 
        tagAnalysis = new OwnerTagJDTAnalysis();
        break;
      default:
        return null;
    }
    PersistentClass persistentClass = tagAnalysis.analyze(publicTypeDecl , currentAnnotations , imports);
    persistentClass.setClassName(fullClassName);
    persistentClass.setPkage(packageName);
    return persistentClass;
  }
  
  /**
   * 验证类标识是否符合建立PersistentClass对象的标准
   * @param currentAnnotations 
   * @param currentModifiers 
   * @param imports 
   * @return 不同的验证结果有不同的返回值：0：标识这个被验证的类不满足建立PersistentClass对象的条件；<br>
   * 1：表示这个类的PersistentClass对象标识采用JPA体系进行描述<br>
   * 2：表示这个类的PersistentClass对象标识采用骨架V3.0自有标签体系进行描述<br>
   * TODO 未测试
   */
  private int validateClassType(List<Annotation> currentAnnotations , List<Modifier> currentModifiers , List<ImportDeclaration> imports) {
    /*
     * 符合建立PersistentClass对象的标准需要满足的条件是
     * 1、类修饰符必须是public
     * 2、必须有@javax.persistence.Entity注解或者
     * @com.vanda.platform.saturn.core.engine.annotation.SaturnEntity注解
     * */
    boolean isGoodModifier = false;
    boolean isJpaAnnotation = false;
    boolean isTagAnnotation = false;
    // 1、===================================
    for (Modifier modifier : currentModifiers) {
      if(StringUtils.equals("public", modifier.getKeyword().toString())) {
        isGoodModifier = true;
      }
    }
    
    // 2、===================================
    AnnoTODO:for(Annotation annotation : currentAnnotations) {
      String souceTypeName = annotation.getTypeName().toString();
      // 如果条件成立，说明这个annotation使用的不是完整的类名
      if(souceTypeName.indexOf(".") == -1 && souceTypeName.equals("Entity")) {
        for(ImportDeclaration importDeclaration : imports) {
          // 如果条件成立，说明这是一个完整的类引用
          if(!importDeclaration.isOnDemand()) {
            String importTypeName = importDeclaration.getName().toString();
            if(StringUtils.equals("javax.persistence.Entity", importTypeName)) {
              isJpaAnnotation = true;
              break AnnoTODO;
            }
          } 
          // 否则就是一个带有“*”的不完整类引用
          else {
            String importTypeName = importDeclaration.getName().toString();
            if(StringUtils.equals("javax.persistence", importTypeName)) {
              isJpaAnnotation = true;
              break AnnoTODO;
            }
          }
        }
      }
      // 类似，详细过程参见以上的if块
      else if(souceTypeName.indexOf(".") == -1 && souceTypeName.equals("SaturnEntity")) {
        for(ImportDeclaration importDeclaration : imports) {
          // 如果条件成立，说明这是一个完整的类引用
          if(!importDeclaration.isOnDemand()) {
            String importTypeName = importDeclaration.getName().toString();
            if(StringUtils.equals("com.vanda.platform.saturn.core.engine.annotation.SaturnEntity", importTypeName)) {
              isJpaAnnotation = true;
              break AnnoTODO;
            }
          } 
          // 否则就是一个带有“*”的不完整类引用
          else {
            String importTypeName = importDeclaration.getName().toString();
            if(StringUtils.equals("com.vanda.platform.saturn.core.engine.annotation", importTypeName)) {
              isJpaAnnotation = true;
              break AnnoTODO;
            }
          }
        }
      }
      else if(souceTypeName.indexOf(".") != -1 && souceTypeName.equals("javax.persistence.Entity")) {
        isJpaAnnotation = true;
        break AnnoTODO;
      }
      else if(souceTypeName.indexOf(".") != -1 && souceTypeName.equals("com.vanda.platform.saturn.core.engine.annotation.SaturnEntity")) {
        isTagAnnotation = true;
        break AnnoTODO;
      } 
    }
    
    if(isGoodModifier) {
      return 0;
    } else if(isJpaAnnotation) {
      return 1;
    } else if(isTagAnnotation) {
      return 2;
    } else {
      return 0;
    }
  }
}