/*
 * Created on Jan 17, 2005 
 *
 * $Id$
 */
package net.sourceforge.pmd.rules.sunsecure;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.ast.ASTAssignmentOperator;
import net.sourceforge.pmd.ast.ASTBlockStatement;
import net.sourceforge.pmd.ast.ASTConstructorDeclaration;
import net.sourceforge.pmd.ast.ASTExpression;
import net.sourceforge.pmd.ast.ASTFormalParameter;
import net.sourceforge.pmd.ast.ASTFormalParameters;
import net.sourceforge.pmd.ast.ASTInterfaceDeclaration;
import net.sourceforge.pmd.ast.ASTMethodDeclaration;
import net.sourceforge.pmd.ast.ASTPrimaryExpression;
import net.sourceforge.pmd.ast.ASTPrimarySuffix;
import net.sourceforge.pmd.ast.ASTStatementExpression;
import net.sourceforge.pmd.ast.ASTVariableDeclaratorId;

/**
 * 
 * @author mgriffa
 */
public class ArrayIsStoredDirectly extends AbstractSunSecureRule {

    public Object visit(ASTInterfaceDeclaration decl, Object data) {
        return data; // just skip interfaces
    }

    /**
     * Overriden method.
     * 
     * @see net.sourceforge.pmd.ast.JavaParserVisitor#visit(net.sourceforge.pmd.ast.ASTConstructorDeclaration, java.lang.Object)
     */
    public Object visit(ASTConstructorDeclaration node, Object data) {
        ASTFormalParameter[] arrs = getArrays((ASTFormalParameters) node.jjtGetChild(0));
        if (arrs!=null) {
            //TODO check if one of these arrays is stored in a non local variable
            List bs = node.findChildrenOfType(ASTBlockStatement.class);
            checkDirectlyAssigned((RuleContext)data, arrs, bs);
        }
        return data;
    }
    
    private void checkDirectlyAssigned(RuleContext context, ASTFormalParameter[] arrs, List bs) {
        for (int i=0;i<arrs.length;i++) {
            if (isDirectlyAssigned(arrs[i], bs)) {
                addViolation(context, arrs[i].getBeginLine());
            }   
        }
    }
    
    /**
     * Checks if the variable designed in parameter is written to a field (not local variable) in the statements.
     */
    private boolean isDirectlyAssigned(final ASTFormalParameter parameter, final List bs) {
        final ASTVariableDeclaratorId vid = (ASTVariableDeclaratorId) parameter.getFirstChildOfType(ASTVariableDeclaratorId.class);
        final String varName = vid.getImage();
        for (Iterator it = bs.iterator() ; it.hasNext() ; ) {
            final ASTBlockStatement b = (ASTBlockStatement) it.next();
            if (b.containsChildOfType(ASTAssignmentOperator.class)) {
                final ASTStatementExpression se = (ASTStatementExpression) b.getFirstChildOfType(ASTStatementExpression.class);
                ASTPrimaryExpression pe = (ASTPrimaryExpression) se.jjtGetChild(0);
                String assignedVar = getFirstNameImage(pe);
                if (assignedVar==null) {
                    assignedVar = ((ASTPrimarySuffix)se.getFirstChildOfType(ASTPrimarySuffix.class)).getImage();
                }
                
                if (!isLocalVariable(assignedVar, (ASTMethodDeclaration) pe.getFirstParentOfType(ASTMethodDeclaration.class))) {
                    
                    ASTExpression e = (ASTExpression) se.jjtGetChild(2);
                    String val = getFirstNameImage(e);
                    if (val==null) {
                        val = ((ASTPrimarySuffix)se.getFirstChildOfType(ASTPrimarySuffix.class)).getImage();
                    }
                    
                    if (val.equals(varName) 
                            && !isLocalVariable(varName, (ASTMethodDeclaration) parameter.getFirstParentOfType(ASTMethodDeclaration.class))) {
                        return true;
                    }
                }
            }            
        }
        return false;
    }

    public Object visit(ASTMethodDeclaration node, Object data) {
        final ASTFormalParameters params = (ASTFormalParameters) node.getFirstChildOfType(ASTFormalParameters.class);
        ASTFormalParameter[] arrs = getArrays(params);
        if (arrs!=null) {
            List bs = node.findChildrenOfType(ASTBlockStatement.class);
            checkDirectlyAssigned((RuleContext)data, arrs, bs);
        }
        return data;
    }

    private final ASTFormalParameter[] getArrays(ASTFormalParameters params) {
        final List l = params.findChildrenOfType(ASTFormalParameter.class);
        if (l!=null && !l.isEmpty()) {
            Vector v = new Vector();
            for (Iterator it = l.iterator() ; it.hasNext() ; ) {
                ASTFormalParameter fp = (ASTFormalParameter) it.next();
                if (fp.isArray())
                    v.add(fp);
            }
            return (ASTFormalParameter[]) v.toArray(new ASTFormalParameter[v.size()]);
        }
        return null;
    }

}
