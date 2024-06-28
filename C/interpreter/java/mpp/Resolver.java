package mpp;

import static mpp.TokenType.SUPER;
import static mpp.TokenType.THIS;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import mpp.Expr.Assign;
import mpp.Expr.Binary;
import mpp.Expr.Call;
import mpp.Expr.Get;
import mpp.Expr.Grouping;
import mpp.Expr.Lambda;
import mpp.Expr.Literal;
import mpp.Expr.Logical;
import mpp.Expr.MList;
import mpp.Expr.Set;
import mpp.Expr.Super;
import mpp.Expr.Ternary;
import mpp.Expr.This;
import mpp.Expr.Unary;
import mpp.Expr.Variable;
import mpp.Stmt.Block;
import mpp.Stmt.Break;
import mpp.Stmt.Class;
import mpp.Stmt.Continue;
import mpp.Stmt.Expression;
import mpp.Stmt.Function;
import mpp.Stmt.If;
import mpp.Stmt.Loop;
import mpp.Stmt.Return;
import mpp.Stmt.Var;

class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {
    private enum VarState {
        USED, UNUSED
    }

    private enum ClassType {
        NONE, CLASS, SUBCLASS
    }

    private enum FunctionType {
        FUNCTION, GETTER, METHOD, INIT, NONE
    }

    private class LocalVariable {
        VarState state;
        Token name;

        LocalVariable(Token name, VarState state) {
            this.name = name;
            this.state = state;
        }
    }

    private boolean inLoop = false;
    private ClassType currentClass = ClassType.NONE;
    private FunctionType currentFunction = FunctionType.NONE;
    private final Interpreter interpreter;
    private Stack<Map<String, LocalVariable>> scopes = new Stack<>();

    Resolver(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void resolve(List<Stmt> stmts) {
        for (Stmt stmt : stmts)
            resolve(stmt);
    }

    @Override
    public Void visitBlock(Block stmt) {
        startScope();
        resolve(stmt.statements);
        endScope();
        return null;
    }

    @Override
    public Void visitLoop(Loop stmt) {
        inLoop = true;
        resolve(stmt.condition);
        resolve(stmt.whileStmt);
        if (stmt.increment != null)
            resolve(stmt.increment);
        inLoop = false;
        return null;
    }

    @Override
    public Void visitIf(If stmt) {
        resolve(stmt.condition);
        resolve(stmt.trueStmt);
        if (stmt.falseStmt != null)
            resolve(stmt.falseStmt);
        return null;
    }

    @Override
    public Void visitVar(Var stmt) {
        if (stmt.initializer != null)
            resolve(stmt.initializer);

        declare(stmt.name);
        // define(stmt.name);
        return null;
    }

    @Override
    public Void visitExpression(Expression stmt) {
        resolve(stmt.expression);
        return null;
    }

    /*
     * private void define(Token name) {
     * if (scopes.isEmpty())
     * return;
     * scopes.peek().put(name.lexeme, true);
     * }
     */

    @Override
    public Void visitBreak(Break stmt) {
        if (!inLoop)
            Minhpp.error(stmt.name, "Not in loop statement.");
        return null;
    }

    @Override
    public Void visitContinue(Continue stmt) {
        if (!inLoop)
            Minhpp.error(stmt.name, "Not in loop statement.");
        return null;
    }

    @Override
    public Void visitReturn(Return stmt) {
        if (currentFunction == FunctionType.NONE) {
            Minhpp.error(stmt.name, "Can't return from top-level code.");
        }

        if (stmt.value != null) {
            if (currentFunction == FunctionType.INIT)
                Minhpp.error(stmt.name, "Can't return a value from an initializer.");
            resolve(stmt.value);
        }
        return null;
    }

    @Override
    public Void visitFunction(Function stmt) {
        declare(stmt.name);
        // define(stmt.name);

        resolveFunction(stmt, FunctionType.FUNCTION);
        return null;
    }

    @Override
    public Void visitBinary(Binary expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitAssign(Assign expr) {
        resolve(expr.value);
        resolveLocal(expr, expr.name, false);
        return null;
    }

    @Override
    public Void visitTernary(Ternary expr) {
        resolve(expr.condition);
        resolve(expr.trueExpr);
        resolve(expr.falseExpr);
        return null;
    }

    @Override
    public Void visitLogical(Logical expr) {
        resolve(expr.left);
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitLambda(Lambda expr) {
        return null;
    }

    @Override
    public Void visitGrouping(Grouping expr) {
        resolve(expr.expression);
        return null;
    }

    @Override
    public Void visitLiteral(Literal expr) {
        return null;
    }

    @Override
    public Void visitCall(Call expr) {
        resolve(expr.callee);
        for (Expr e : expr.arguments) {
            resolve(e);
        }
        return null;
    }

    @Override
    public Void visitVariable(Variable expr) {
        /*
         * if (!scopes.empty() && scopes.peek().get(expr.name.lexeme) ==
         * LocalVariable.FALSE)
         * {
         * Minhpp.error(expr.name, "Can't read local variable in its own initializer");
         * }
         */

        resolveLocal(expr, expr.name, true);
        return null;
    }

    @Override
    public Void visitUnary(Unary expr) {
        resolve(expr.right);
        return null;
    }

    @Override
    public Void visitClass(Class stmt) {
        ClassType enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(stmt.name);

        if (stmt.superclass != null) {
            if (stmt.superclass.name.lexeme.equals(stmt.name.lexeme))
                Minhpp.error(stmt.superclass.name, "A class can't inherit from itself.");
            else {
                currentClass = ClassType.SUBCLASS;
                resolve(stmt.superclass);
                startScope();
                scopes.peek().put("super",
                        new LocalVariable(
                                new Token(SUPER, "super", null, 0),
                                VarState.USED));
            }
        }

        startScope();
        scopes.peek().put("this",
                new LocalVariable(
                        new Token(THIS, "this", null, 0),
                        VarState.USED));

        for (Stmt.Function method : stmt.statics) {
            if (method.params == null)
                resolveFunction(method, FunctionType.GETTER);
            else
                resolveFunction(method, FunctionType.METHOD);
        }

        for (Stmt.Function method : stmt.methods) {
            if (method.name.lexeme.equals(stmt.name.lexeme))
                resolveFunction(method, FunctionType.INIT);
            else if (method.params == null)
                resolveFunction(method, FunctionType.GETTER);
            else
                resolveFunction(method, FunctionType.METHOD);
        }

        endScope();

        if (stmt.superclass != null) {
            endScope();
        }

        currentClass = enclosingClass;

        return null;
    }

    @Override
    public Void visitGet(Get expr) {
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitSet(Set expr) {
        resolve(expr.value);
        resolve(expr.object);
        return null;
    }

    @Override
    public Void visitThis(This expr) {
        if (currentClass == ClassType.NONE) {
            Minhpp.error(expr.keyword, "Can't use 'this' outside a class.");
            return null;
        }

        resolveLocal(expr, expr.keyword, true);
        return null;
    }

    private void resolve(Stmt stmt) {
        stmt.accept(this);
    }

    private void resolve(Expr expr) {
        expr.accept(this);
    }

    private void startScope() {
        scopes.push(new HashMap<String, LocalVariable>());
    }

    private void endScope() {
        if (scopes.isEmpty())
            return;
        Map<String, LocalVariable> scope = scopes.pop();
        for (LocalVariable variable : scope.values()) {
            if (variable.state == VarState.UNUSED)
                Minhpp.warning(variable.name, "Variable " + variable.name.lexeme + " is not used locally.");
        }
    }

    private void declare(Token name) {
        if (scopes.isEmpty())
            return;

        Map<String, LocalVariable> scope = scopes.peek();
        if (scope.containsKey(name.lexeme)) {
            Minhpp.error(name, "A variable with this name already exists in this scope.");
        }
        scope.put(name.lexeme, new LocalVariable(name, VarState.UNUSED));
    }

    private void resolveLocal(Expr expr, Token name, boolean isUsed) {
        for (int i = scopes.size() - 1; i >= 0; --i) {
            Map<String, LocalVariable> scope = scopes.get(i);
            if (scope.containsKey(name.lexeme)) {
                if (isUsed)
                    scope.get(name.lexeme).state = VarState.USED;

                interpreter.resolve(expr, scopes.size() - 1 - i);
                return;
            }
        }
    }

    private void resolveFunction(Function function, FunctionType type) {
        FunctionType enclosingFunction = currentFunction;
        currentFunction = type;
        startScope();

        if (type != FunctionType.GETTER) {
            for (Token param : function.params) {
                declare(param);
                // define(param);
            }
        }

        resolve(function.body);
        endScope();
        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitSuper(Super expr) {
        if (currentClass == ClassType.NONE) {
            Minhpp.error(expr.keyword,
                    "Can't use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            Minhpp.error(expr.keyword,
                    "Can't use 'super' in a class with no superclass.");
        }

        resolveLocal(expr, expr.keyword, true);
        return null;
    }

    @Override
    public Void visitMList(MList expr) {
        for (Expr init : expr.init) {
            resolve(init);
        }

        return null;
    }
}
