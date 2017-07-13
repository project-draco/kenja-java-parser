package jp.naist.sd.kenja.factextractor.ast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class MethodInvocationVisitor extends ASTVisitor {
    @Override
    public boolean visit(MethodInvocation node) {
        File f = new File("dependencies.txt");
    	FileWriter fw = null;
    	BufferedWriter bw = null;
    	try {
    		fw = f.exists() ? new FileWriter(f, true) : new FileWriter(f, true);
    		bw = new BufferedWriter(fw);
    		bw.write("\tCALLED METHOD: " + node.getExpression().resolveTypeBinding().getQualifiedName() + "." + node.getName() + "\n");
    		bw.close();
    	} catch (IOException e) {
    		e.printStackTrace();
    	}
        return true;
    }
    
    
}
