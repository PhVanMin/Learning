package mpp;

import static mpp.TokenType.OR;
import java.util.ArrayList;
import java.util.List;

import mpp.Expr.Assign;
import mpp.Expr.Binary;
import mpp.Expr.Call;
import mpp.Expr.Grouping;
import mpp.Expr.Lambda;
import mpp.Expr.Literal;
import mpp.Expr.Logical;
import mpp.Expr.Ternary;
import mpp.Expr.Unary;
import mpp.Expr.Variable;
import mpp.Stmt.Function;
import mpp.Stmt.If;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final Environment globals = new Environment();
    private Environment environment = globals;
    private boolean cmd;

    final class Break extends RuntimeException {
    }

    final class Continue extends RuntimeException {
    }

    public Interpreter() {
        globals.define("clock", new MinhppCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return (double) System.currentTimeMillis() / 1000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void interpret(List<Stmt> stmts, boolean cmd) {
        this.cmd = cmd;

        try {
            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (RuntimeError error) {
            Minhpp.runtimeError(error);
        }
    }

    @Override
    public Object visitBinary(Binary expr) {
        Object left = evaluate(expr.left);
        Object right = evaluate(expr.right);

        switch (expr.operator.type) {
            case PLUS:
                if (left instanceof Double && right instanceof Double)
                    return (double) left + (double) right;

                if ((left instanceof Double || left instanceof String)
                        && (right instanceof Double || right instanceof String))
                    return stringify(left) + stringify(right);

                throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
            case MINUS:
                checkNumberOperands(expr.operator, left, right);
                return (double) left - (double) right;
            case STAR:
                checkNumberOperands(expr.operator, left, right);
                return (double) left * (double) right;
            case SLASH:
                checkNumberOperands(expr.operator, left, right);

                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Division by 0.");

                return (double) left / (double) right;
            case PERCEN:
                checkNumberOperands(expr.operator, left, right);

                if ((double) right == 0)
                    throw new RuntimeError(expr.operator, "Division by 0.");

                return (double) left % (double) right;
            case COMMA:
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

    @Override
    public Void visitExpression(Stmt.Expression stmt) {
        Object value = evaluate(stmt.expression);
        if (cmd)
            System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitPrint(Stmt.Print stmt) {
        Object value = evaluate(stmt.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitVar(Stmt.Var stmt) {
        environment.checkDefine(stmt.name);
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariable(Variable expr) {
        Object value = environment.get(expr.name);
        if (value != null)
            return value;

        throw new RuntimeError(expr.name, "Expected non-null value of variable.");
    }

    @Override
    public Object visitAssign(Assign expr) {
        Object value = evaluate(expr.value);
        environment.assign(expr.name, value);
        return value;
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
    }

    private void execute(Stmt statement) {
        statement.accept(this);
    }

    private String stringify(Object obj) {
        if (obj == null)
            return "nil";

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

    private void checkNumberOperands(Token operator, Object... operands) {
        for (Object operand : operands) {
            if (!(operand instanceof Double))
                throw new RuntimeError(operator, "Operand must be a number.");
        }
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null)
            return true;
        if (a == null)
            return false;

        return a.equals(b);
    }

    private boolean isTruthy(Object obj) {
        if (obj instanceof Boolean)
            return (boolean) obj;

        if (obj != null)
            return true;

        return false;
    }

    public void executeBlock(List<Stmt> stmts, Environment env) {
        Environment prevEnv = environment;

        try {
            environment = env;

            for (Stmt stmt : stmts) {
                execute(stmt);
            }
        } catch (Break | Continue bc) {
            throw bc;
        } finally {
            environment = prevEnv;
        }
    }

    @Override
    public Void visitIf(If stmt) {
        if (isTruthy(evaluate(stmt.condition))) {
            execute(stmt.trueStmt);
        } else if (stmt.falseStmt != null) {
            execute(stmt.falseStmt);
        }
        return null;
    }

    @Override
    public Object visitLogical(Logical expr) {
        Object left = evaluate(expr.left);

        if (expr.operator.type == OR) {
            if (isTruthy(left))
                return left;
        } else {
            if (!isTruthy(left))
                return left;
        }

        return evaluate(expr.right);
    }

    @Override
    public Void visitLoop(Stmt.Loop stmt) {
        while (isTruthy(evaluate(stmt.condition))) {
            try {
                execute(stmt.whileStmt);
                if (stmt.increment != null)
                    execute(stmt.increment);
            } catch (Break b) {
                break;
            } catch (Continue c) {
                if (stmt.increment != null)
                    execute(stmt.increment);
            }
        }

        return null;
    }

    @Override
    public Void visitBreak(Stmt.Break stmt) {
        throw new Break();
    }

    @Override
    public Void visitContinue(Stmt.Continue stmt) {
        throw new Continue();
    }

    @Override
    public Object visitCall(Call expr) {
        Object callee = evaluate(expr.callee);

        List<Object> arguments = new ArrayList<>();
        for (Expr arg : expr.arguments) {
            arguments.add(evaluate(arg));
        }

        if (!(callee instanceof MinhppCallable)) {
            throw new RuntimeError(expr.paren, "Can only call functions and classes.");
        }

        MinhppCallable function = (MinhppCallable) callee;
        if (arguments.size() != function.arity()) {
            throw new RuntimeError(expr.paren,
                    "Expected " + function.arity() + " arguments but got " + arguments.size() + ".");
        }
        return function.call(this, arguments);
    }

    @Override
    public Void visitFunction(Function stmt) {
        MinhppFunction function = new MinhppFunction(stmt, environment);
        environment.define(stmt.name.lexeme, function);
        return null;
    }

    @Override
    public Void visitReturn(Stmt.Return stmt) {
        Object value = null;

        if (stmt.value != null)
            value = evaluate(stmt.value);

        throw new mpp.Return(value);
    }

    @Override
    public Object visitLambda(Lambda expr) {
        return new MinhppFunction(expr.function, environment);
    }
}
