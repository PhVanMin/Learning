package mpp;

import static mpp.TokenType.OR;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import mpp.Expr.Assign;
import mpp.Expr.Binary;
import mpp.Expr.Call;
import mpp.Expr.Get;
import mpp.Expr.Grouping;
import mpp.Expr.Lambda;
import mpp.Expr.Literal;
import mpp.Expr.Logical;
import mpp.Expr.MList;
import mpp.Expr.Postfix;
import mpp.Expr.Set;
import mpp.Expr.Super;
import mpp.Expr.Ternary;
import mpp.Expr.This;
import mpp.Expr.Unary;
import mpp.Expr.Variable;
import mpp.Stmt.Class;
import mpp.Stmt.Function;
import mpp.Stmt.If;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
    final class Break extends RuntimeException {
    }

    final class Continue extends RuntimeException {
    }

    final Environment globals = new Environment();
    private Environment environment = globals;
    private boolean cmd;
    private final Map<Expr, Integer> locals = new HashMap<>();

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

        globals.define("input", new MinhppCallable() {
            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                Scanner sc = new Scanner(System.in);
                String input = sc.nextLine();
                sc.close();
                return input;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("println", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.println(stringify(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        globals.define("print", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                System.out.print(stringify(args.get(0)));
                return null;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });
    }

    public void resolve(Expr expr, int depth) {
        locals.put(expr, depth);
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

                if (left instanceof String || right instanceof String)
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
            case MINUS_MINUS:
                checkNumberOperands(expr.operator, right);
                if (expr.right instanceof Expr.Variable) {
                    Token name = ((Expr.Variable) expr.right).name;

                    Integer distance = locals.get(expr.right);
                    if (distance != null) {
                        environment.assignAt(distance, name, (double) right - 1);
                    } else {
                        globals.assign(name, (double) right - 1);
                    }
                } else if (expr.right instanceof Expr.Get) {
                    Expr.Get instance = (Expr.Get) expr.right;
                    Object object = evaluate(instance.object);

                    if (!(object instanceof MinhppInstance)) {
                        throw new RuntimeError(instance.name, "Only instances have property.");
                    }

                    ((MinhppInstance) object).set(instance.name, (double) right - 1);
                }

                return (double) right - 1;
            case PLUS_PLUS:
                checkNumberOperands(expr.operator, right);

                if (expr.right instanceof Expr.Variable) {
                    Token name = ((Expr.Variable) expr.right).name;

                    Integer distance = locals.get(expr.right);
                    if (distance != null) {
                        environment.assignAt(distance, name, (double) right + 1);
                    } else {
                        globals.assign(name, (double) right + 1);
                    }
                } else if (expr.right instanceof Expr.Get) {
                    Expr.Get instance = (Expr.Get) expr.right;
                    Object object = evaluate(instance.object);

                    if (!(object instanceof MinhppInstance)) {
                        throw new RuntimeError(instance.name, "Only instances have property.");
                    }

                    ((MinhppInstance) object).set(instance.name, (double) right + 1);
                }

                return (double) right + 1;
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
    public Void visitVar(Stmt.Var stmt) {
        Object value = null;

        if (stmt.initializer != null) {
            value = evaluate(stmt.initializer);
        }

        environment.define(stmt.name.lexeme, value);
        return null;
    }

    @Override
    public Object visitVariable(Variable expr) {
        return lookUpVariable(expr.name, expr);
    }

    @Override
    public Object visitAssign(Assign expr) {
        Object value = evaluate(expr.value);

        Integer distance = locals.get(expr);
        if (distance != null) {
            environment.assignAt(distance, expr.name, value);
        } else {
            globals.assign(expr.name, value);
        }

        return value;
    }

    @Override
    public Void visitBlock(Stmt.Block stmt) {
        executeBlock(stmt.statements, new Environment(environment));
        return null;
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
        MinhppFunction function = new MinhppFunction(stmt, environment, false);
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
        return new MinhppFunction(expr.function, environment, false);
    }

    @Override
    public Void visitClass(Class stmt) {
        Object superclass = null;
        if (stmt.superclass != null) {
            superclass = evaluate(stmt.superclass);
            if (!(superclass instanceof MinhppClass))
                throw new RuntimeError(stmt.superclass.name, "Superclass must be a class.");
        }

        Map<String, MinhppFunction> statics = new HashMap<>();
        Map<String, MinhppFunction> methods = new HashMap<>();
        MinhppClass mClass;

        environment.define(stmt.name.lexeme, null);
        // define super for static methods
        if (superclass != null) {
            environment = new Environment(environment);
            environment.define("super", ((MinhppClass) superclass).mClass);
        }

        for (Stmt.Function method : stmt.statics) {
            MinhppFunction function = new MinhppFunction(method, environment,
                    false);
            statics.put(method.name.lexeme, function);
        }

        if (superclass != null) {
            environment = environment.enclosing;
        }
        // end define for static methods

        // define super for class methods
        if (superclass != null) {
            environment = new Environment(environment);
            environment.define("super", superclass);
        }

        for (Stmt.Function method : stmt.methods) {
            MinhppFunction function = new MinhppFunction(method, environment,
                    method.name.lexeme.equals(stmt.name.lexeme));
            methods.put(method.name.lexeme, function);
        }

        if (superclass != null) {
            environment = environment.enclosing;
        }
        // end define for class methods

        if (superclass == null) {
            MinhppClass metaclass = new MinhppClass(null, null, "Meta " + stmt.name.lexeme, statics);
            mClass = new MinhppClass(metaclass, null, stmt.name.lexeme, methods);
        } else {
            MinhppClass metaclass = new MinhppClass(null,
                    ((MinhppClass) superclass).mClass,
                    "Meta " + stmt.name.lexeme, statics);
            mClass = new MinhppClass(metaclass, (MinhppClass) superclass, stmt.name.lexeme, methods);
        }

        environment.assign(stmt.name, mClass);
        return null;
    }

    @Override
    public Object visitGet(Get expr) {
        Object object = evaluate(expr.object);

        if (!(object instanceof MinhppInstance)) {
            throw new RuntimeError(expr.name, "Only instances have property.");
        }

        Object result = ((MinhppInstance) object).get(expr.name);
        if (result instanceof MinhppFunction && ((MinhppFunction) result).arity() == -1)
            return ((MinhppFunction) result).call(this, null);

        return result;
    }

    @Override
    public Object visitSet(Set expr) {
        Object object = evaluate(expr.object);

        if (object instanceof MinhppInstance) {
            Object value = evaluate(expr.value);
            ((MinhppInstance) object).set(expr.name, value);
            return value;
        }

        throw new RuntimeError(expr.name, "Only instances have fields.");
    }

    @Override
    public Object visitThis(This expr) {
        return lookUpVariable(expr.keyword, expr);
    }

    private Object lookUpVariable(Token name, Expr expr) {
        Integer distance = locals.get(expr);
        if (distance != null) {
            return environment.getAt(distance, name.lexeme);
        } else {
            return globals.get(name);
        }
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

    @Override
    public Object visitSuper(Super expr) {
        int distance = locals.get(expr);
        MinhppClass superclass = (MinhppClass) environment.getAt(distance, "super");
        MinhppFunction method = superclass.findMethod(expr.method.lexeme);
        if (method == null)
            throw new RuntimeError(expr.method, "Undefined property '" + expr.method.lexeme + "'.");

        MinhppInstance currentInstance = (MinhppInstance) environment.getAt(distance - 1, "this");
        if (method.arity() == -1)
            return method.bind(currentInstance).call(this, null);

        return method.bind(currentInstance);
    }

    @Override
    public Object visitMList(MList expr) {
        List<Object> items = new ArrayList<>();
        for (Expr init : expr.init) {
            items.add(evaluate(init));
        }
        return new MinhppList(items);
    }

    @Override
    public Object visitPostfix(Postfix expr) {
        Object left = evaluate(expr.left);
        switch (expr.operator.type) {
            case MINUS_MINUS:
                checkNumberOperands(expr.operator, left);
                if (expr.left instanceof Expr.Variable) {
                    Token name = ((Expr.Variable) expr.left).name;

                    Integer distance = locals.get(expr.left);
                    if (distance != null) {
                        environment.assignAt(distance, name, (double) left - 1);
                    } else {
                        globals.assign(name, (double) left - 1);
                    }
                } else if (expr.left instanceof Expr.Get) {
                    Expr.Get instance = (Expr.Get) expr.left;
                    Object object = evaluate(instance.object);

                    if (!(object instanceof MinhppInstance)) {
                        throw new RuntimeError(instance.name, "Only instances have property.");
                    }

                    ((MinhppInstance) object).set(instance.name, (double) left - 1);
                }

                return (double) left;
            case PLUS_PLUS:
                checkNumberOperands(expr.operator, left);

                if (expr.left instanceof Expr.Variable) {
                    Token name = ((Expr.Variable) expr.left).name;

                    Integer distance = locals.get(expr.left);
                    if (distance != null) {
                        environment.assignAt(distance, name, (double) left + 1);
                    } else {
                        globals.assign(name, (double) left + 1);
                    }
                } else if (expr.left instanceof Expr.Get) {
                    Expr.Get instance = (Expr.Get) expr.left;
                    Object object = evaluate(instance.object);

                    if (!(object instanceof MinhppInstance)) {
                        throw new RuntimeError(instance.name, "Only instances have property.");
                    }

                    ((MinhppInstance) object).set(instance.name, (double) left + 1);
                }

                return left;
            default:
                break;
        }

        return null;
    }
}
