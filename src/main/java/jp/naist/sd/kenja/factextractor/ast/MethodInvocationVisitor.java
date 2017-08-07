package jp.naist.sd.kenja.factextractor.ast;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

import java.util.Collection;

public class MethodInvocationVisitor extends ASTVisitor {
	private Collection<String> called;
	public MethodInvocationVisitor(Collection<String> called) {
		this.called = called;
	}

	@Override
    public boolean visit(MethodInvocation node) {
		if (node == null || node.getExpression() == null || node.getExpression().resolveTypeBinding() == null ) {
			return true;
		}
		called.add(node.getExpression().resolveTypeBinding().getQualifiedName() + "." + node.getName());
        return true;
    }

}
