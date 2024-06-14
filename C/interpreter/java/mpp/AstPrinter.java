package mpp;

import mpp.Expr.Assign;
import mpp.Expr.Binary;
import mpp.Expr.Grouping;
import mpp.Expr.Literal;
import mpp.Expr.Logical;
import mpp.Expr.Ternary;
import mpp.Expr.Unary;
import mpp.Expr.Variable;

public class AstPrinter implements Expr.Visitor<String> {
    public String print(Expr expr) {
        if (expr == null)
            return "";
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

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ").append(expr.accept(this));
        }

        builder.append(")");

        return builder.toString();
    }

    @Override
    public String visitTernary(Ternary expr) {
        return parenthesize("tenary", expr.condition, expr.trueExpr, expr.falseExpr);
    }

	@Override
	public String visitAssign(Assign expr) {
        return parenthesize("assign " + expr.name.lexeme, expr.value);
	}

	@Override
	public String visitLogical(Logical expr) {
        return parenthesize(expr.operator.lexeme, expr.left, expr.right);
	}

	@Override
	public String visitVariable(Variable expr) {
        return parenthesize("get " + expr.name.lexeme);
	}
}
