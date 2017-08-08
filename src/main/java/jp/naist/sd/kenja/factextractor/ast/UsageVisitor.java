package jp.naist.sd.kenja.factextractor.ast;

import jp.naist.sd.kenja.factextractor.SourceFinder;
import org.eclipse.jdt.core.dom.*;

import java.util.Collection;

public class UsageVisitor extends ASTVisitor {
	private Collection<String> called;
	public UsageVisitor(Collection<String> called) {
		this.called = called;
	}

	@Override
    public boolean visit(MethodInvocation node) {
		if (node == null || node.getExpression() == null
                || node.getExpression().resolveTypeBinding() == null ) {
			return true;
		}
        String qualifiedName = node.getExpression().resolveTypeBinding().getQualifiedName();
        String fileName = SourceFinder.getInstance().findSource(qualifiedName);
        String treeName = getTreeName(node.resolveMethodBinding().getMethodDeclaration());
		called.add(fileName + "/[MT]/" + treeName + "/body");
        return true;
    }

    private String getTreeName(IMethodBinding node) {
        StringBuilder result = new StringBuilder(node.getName());
        result.append("(");
        for (ITypeBinding item : node.getParameterTypes()) {
            result.append(item.getName());
            result.append(",");
        }
        int lastIndex = result.lastIndexOf(",");
        if (lastIndex > 0) {
            result.deleteCharAt(lastIndex);
        }
        result.append(")");
        return result.toString();
    }

    @Override
    public boolean visit(FieldAccess node) {
        if (node == null || node.getExpression() == null
                || node.getExpression().resolveTypeBinding() == null ) {
            return true;
        }
        String qualifiedName = node.getExpression().resolveTypeBinding().getQualifiedName();
        String fileName = SourceFinder.getInstance().findSource(qualifiedName);
        called.add(fileName + "/[FE]/" + node.getName());
        return true;
    }
}
