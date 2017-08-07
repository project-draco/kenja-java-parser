package jp.naist.sd.kenja.factextractor.ast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import jp.naist.sd.kenja.factextractor.Blob;
import jp.naist.sd.kenja.factextractor.Tree;
import jp.naist.sd.kenja.factextractor.Treeable;

/**
 * A class which represents Method of Java for Historage.
 * 
 * @author Kenji Fujiwara
 *
 */
public class ASTMethod implements Treeable {

  /**
   * A Blob instance corresponding to method body.
   */
  private Blob body;

  /**
   * A Blob instance corresponding to method parameters.
   */
  private Blob parameters;

  /**
   * root Tree of a Method.
   */
  private Tree root;

  /**
   * file name of method body.
   */
  private static final String BODY_BLOB_NAME = "body";

  /**
   * file name of method parameter.
   */
  private static final String PARAMETERS_BLOB_NAME = "parameters";

  /**
   * True if method is a constructor.
   */
  private boolean isConstructor;

  /**
   * Directory name of root tree (method signature).
   */
  private String rootTreeName;

  /**
   * Default constructor of ASTMethod.
   */
  protected ASTMethod() {

  }

  /**
   * Factory method of ASTMethod from MethodDeclaration of Eclipse AST.
   * 
   * @param node
   *          MethodDeclaration of Eclipse AST
   */
  protected ASTMethod(MethodDeclaration node) {
    rootTreeName = getTreeName(node);
    root = new Tree(rootTreeName);

    isConstructor = node.isConstructor();
    setBody(node);
    setParameters(node.parameters());
    setDependencies(node);
  }
  
  private void setDependencies(MethodDeclaration node) {
    Collection<String> called = new HashSet<String>();
   	MethodInvocationVisitor invocationVisitor = new MethodInvocationVisitor(called);
  	node.getBody().accept(invocationVisitor);
    String caller =  node.resolveBinding().getDeclaringClass().getQualifiedName() + "." + node.getName();
    for (String m : called) {
      System.out.println(caller + "\t" + m + "\n");
    }
  }

/**
   * Return root tree name.
   * 
   * @param node
   *          MethodDeclaration of Eclipse AST
   * @return name of root tree
   */
  private String getTreeName(MethodDeclaration node) {
    StringBuilder result = new StringBuilder(node.getName().toString());
    result.append("(");
    for (Object item : node.parameters()) {
      SingleVariableDeclaration parameter = (SingleVariableDeclaration) item;
      result.append(parameter.getType().toString());
      for (int dimension = 0; dimension < parameter.getExtraDimensions(); dimension++) {
        result.append("[]");
      }
      // result.append(" ");
      // result.append(parameter.getName());
      result.append(",");
    }
    int lastIndex = result.lastIndexOf(",");
    if (lastIndex > 0) {
      result.deleteCharAt(lastIndex);
    }
    result.append(")");
    return result.toString();
  }

  /**
   * Read and set method body to the Blob.
   * 
   * @param node
   *          MethodDeclaration of Eclipse AST
   */
  private void setBody(MethodDeclaration node) {
    body = new Blob(BODY_BLOB_NAME);
    if (node.getBody() == null) {
      body.setBody("");
    } else {
      body.setBody(node.getBody().toString());
    }

    root.append(body);
  }

  /**
   * Read and set method parameters to the Blob.
   * 
   * @param parametersList
   *          list of parameters
   */
  private void setParameters(List parametersList) {
    parameters = new Blob(PARAMETERS_BLOB_NAME);
    root.append(parameters);
    String parameterBody = "";
    for (Object item : parametersList) {
      SingleVariableDeclaration parameter = (SingleVariableDeclaration) item;
      parameterBody += parameter.getType().toString();
      parameterBody += " ";
      parameterBody += parameter.getName();
      for (int dimension = 0; dimension < parameter.getExtraDimensions(); dimension++) {
        parameterBody += "[]";
      }
      parameterBody += "\n";
    }
    parameters.setBody(parameterBody);
  }

  /**
   * return directory name of the method.
   * 
   * @return directory name of the method
   */
  public String getName() {
    return rootTreeName;
  }

  /**
   * avoid conflicting blob name.
   * 
   * @param number
   *          unique number of conflicted method
   */
  public void conflict(int number) {
    StringBuilder builder = new StringBuilder();
    builder.append(rootTreeName);
    builder.append(".conflicted");
    builder.append(number);
    root.setName(builder.toString());
  }

  /**
   * Return True if method is constructor.
   * 
   * @return method is constructor or not.
   */
  public boolean isConstructor() {
    return isConstructor;
  }

  /**
   * Factory method of ASTMethod.
   * 
   * @param node
   *          MethodDeclaration of Eclipse AST
   * @return ASTMethod instance created from MethodDeclaration
   */
  public static ASTMethod fromMethodDeclaralation(MethodDeclaration node) {
    return new ASTMethod(node);
  }

  @Override
  public Tree getTree() {
    return root;
  }
}
