package ksp.kos.ideaplugin.expressions;

import com.intellij.lang.ASTNode;
import ksp.kos.ideaplugin.psi.*;

/**
 * Created on 28/01/16.
 *
 * @author ptasha
 */
public abstract class Expression {
    public static Expression parse(KerboScriptExpr psiExpression) throws SyntaxException {
        if (psiExpression instanceof KerboScriptFactor) {
            return Element.parse(psiExpression);
        } else if (psiExpression instanceof KerboScriptUnaryExpr) {
            return Element.parse(psiExpression);
        } else if (psiExpression instanceof KerboScriptArithExpr) {
            return new Addition((KerboScriptArithExpr) psiExpression);
        } else if (psiExpression instanceof KerboScriptMultdivExpr) {
            return new Multiplication((KerboScriptMultdivExpr) psiExpression);
        } else if (psiExpression instanceof KerboScriptNumber) {
            return new Number((KerboScriptNumber) psiExpression);
        } else if (psiExpression instanceof KerboScriptSciNumber) {
            return new Number((KerboScriptSciNumber) psiExpression);
        } else if (psiExpression instanceof KerboScriptSuffix) {
            return new Constant(psiExpression);
        } else if (psiExpression instanceof KerboScriptSuffixterm) {
            KerboScriptSuffixterm suffixterm = (KerboScriptSuffixterm) psiExpression;
            int tailSize = suffixterm.getSuffixtermTrailerList().size();
            if (tailSize == 0) {
                return Atom.parse(suffixterm.getAtom());
            } else if (tailSize == 1) {
                KerboScriptAtom atom = suffixterm.getAtom();
                ASTNode identifier = atom.getNode().findChildByType(KerboScriptTypes.IDENTIFIER);
                if (identifier !=null) {
                    KerboScriptSuffixtermTrailer trailer = suffixterm.getSuffixtermTrailerList().get(0);
                    if (trailer instanceof KerboScriptFunctionTrailer) {
                        return new Function(identifier.getText(), ((KerboScriptFunctionTrailer) trailer).getArglist().getExprList());
                    }
                }
            }
            return new Constant(suffixterm);
        } else {
            throw new SyntaxException("Unexpected element "+psiExpression+": "+psiExpression.getText());
        }
    }

    public Expression simplify() {
        return this;
    }

    public abstract String getText();

    public abstract Expression differentiate();

    public Expression minus() {
        return new Element(-1, Atom.toAtom(this), Number.ONE);
    }

    public Expression plus(Expression expression) {
        return new Addition(this).plus(expression);
    }

    public Expression minus(Expression expression) {
        return new Addition(this).minus(expression);
    }

    public Expression multiply(Expression expression) {
        return new Multiplication(this).multiply(expression);
    }

    public Expression multiplyLeft(Expression expression) {
        return expression.multiply(this);
    }

    public Expression divide(Expression expression) {
        return new Multiplication(this).divide(expression);
    }

    public Expression power(Expression expression) {
        return new Element(1, Atom.toAtom(this), expression);
    }

    public abstract Expression copy();
}
