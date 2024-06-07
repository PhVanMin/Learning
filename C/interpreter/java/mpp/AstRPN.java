package mpp;

import mpp.Expr.Binary;
import mpp.Expr.Grouping;
import mpp.Expr.Literal;
import mpp.Expr.Unary;
import mpp.Expr.Visitor;

public class AstRPN implements Visitor<String> {
    public String printRPN(Expr expr) {
        return expr.accept(this);
    }

    private String toRPN(String opr, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        for (Expr expr : exprs) {
            builder.append(expr.accept(this));
            builder.append(" ");
        }

        builder.append(opr);

        return builder.toString();
    }

    @Override
    public String visitBinary(Binary expr) {
        return toRPN(expr.operator.lexeme, expr.left, expr.right);
    }

    @Override
    public String visitGrouping(Grouping expr) {
        return toRPN("", expr.expression);
    }

    @Override
    public String visitLiteral(Literal expr) {
        return expr.value != null ? expr.value.toString() : "nil";
    }

    @Override
    public String visitUnary(Unary expr) {
        return toRPN(expr.operator.lexeme, expr.right);
    }
}
