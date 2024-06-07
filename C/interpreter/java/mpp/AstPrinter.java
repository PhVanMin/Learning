package mpp;

import mpp.Expr.Binary;
import mpp.Expr.Grouping;
import mpp.Expr.Literal;
import mpp.Expr.Unary;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        return expr.accept(this);
    }

	@Override
	public String visitBinary(Binary expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitGrouping(Grouping expr) {
        return parenthesize("group", expr.expression);
	}

	@Override
	public String visitLiteral(Literal expr) {
        return expr.value != null ? expr.value.toString() : "nil";
	}

	@Override
	public String visitUnary(Unary expr) {
	    return parenthesize(expr.operator.lexeme, expr.right);
    }

    private String parenthesize(String name, Expr ...exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }

        builder.append(")");

        return builder.toString();
    }
}