package jp.naist.sd.kenja.factextractor.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
    @Override
    public boolean visit(MethodInvocation node) {
        System.out.println(node.getExpression() + " " + node.getExpression().resolveTypeBinding().getQualifiedName());
        return true;
    }
}
