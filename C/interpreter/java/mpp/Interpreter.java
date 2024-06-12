package mpp;

import mpp.Expr.Binary;
import mpp.Expr.Grouping;
import mpp.Expr.Literal;
import mpp.Expr.Ternary;
import mpp.Expr.Unary;

public class Interpreter implements Expr.Visitor<Object> {
    public void interpret(Expr expr) {
        try {
            Object value = evaluate(expr);
            System.out.println(stringify(value));
        } catch (RuntimeError error) {
            Minhpp.runtimeError(error);
        }
    }

private String stringify(Object obj) {
    if (obj == null) return "nil";

    if (obj instanceof Double) {
        String text = obj.toString();

        if (text.endsWith(".0")) {
            return text.substring(0, text.length() - 2);
        }
    }

    return obj.toString();
}

    private Object evaluate(Expr expression) {
        return expression.accept(this);
    }

    @Override
    public Object visitBinary(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left - (double) right;

                if (left instanceof String && right instanceof String)
                    return (String) left + (String) right;

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);
                return (double) left / (double) right;
            case COLON:
                return right;
            case EQUAL_EQUAL:
                return isEqual(left, right);
            case BANG_EQUAL:
                return !isEqual(left, right);
            case LESS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left <= (double) right;
            case GREATER:
                checkNumberOperands(expr.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expr.operator, left, right);
                return (double) left >= (double) right;
            default:
                break;
        }

        return null;
    }

    @Override
    public Object visitTernary(Ternary expr) {
        Object condition = evaluate(expr.condition);

        if (isTruthy(condition)) {
            return evaluate(expr.trueExpr);
        } else {
            return evaluate(expr.falseExpr);
        }
    }

    @Override
    public Object visitGrouping(Grouping expr) {
        return evaluate(expr.expression);
    }

    @Override
    public Object visitLiteral(Literal expr) {
        return expr.value;
    }

    @Override
    public Object visitUnary(Unary expr) {
        Object right = expr.right.accept(this);

        switch (expr.operator.type) {
            case MINUS:
                checkNumberOperands(expr.operator, right);
                return -(double) right;
            case BANG:
                return !isTruthy(right);
            default:
                break;
        }

        return null;
    }

    private void checkNumberOperands(Token operator, Object... operands) {
        for (Object operand : operands) {
            if (!(operand instanceof Double))
                throw new RuntimeError(operator, "Operand must be a number.");
        }

    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b ==  null) return true;
        if (a == null) return false;

        return a.equals(b);
    }

    private boolean isTruthy(Object obj) {
        if (obj instanceof Boolean)
            return (boolean) obj;
        if (obj != null)
            return true;
        return false;
    }
}
