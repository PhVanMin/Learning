package mpp;

import java.util.List;

public abstract class Stmt {
    interface Visitor<T> {
        T visitBlock(Block stmt);
        T visitLoop(Loop stmt);
        T visitIf(If stmt);
        T visitClass(Class stmt);
        T visitVar(Var stmt);
        T visitExpression(Expression stmt);
        T visitBreak(Break stmt);
        T visitContinue(Continue stmt);
        T visitReturn(Return stmt);
        T visitFunction(Function stmt);
        T visitPrint(Print stmt);
    }

    abstract <T> T accept(Visitor<T> visitor);

    public static class Block extends Stmt {
        final List<Stmt> statements;
        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlock(this);
        }
    }

    public static class Loop extends Stmt {
        final Expr condition;
        final Stmt whileStmt;
        final Stmt increment;
        public Loop(Expr condition, Stmt whileStmt, Stmt increment) {
            this.condition = condition;
            this.whileStmt = whileStmt;
            this.increment = increment;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLoop(this);
        }
    }

    public static class If extends Stmt {
        final Expr condition;
        final Stmt trueStmt;
        final Stmt falseStmt;
        public If(Expr condition, Stmt trueStmt, Stmt falseStmt) {
            this.condition = condition;
            this.trueStmt = trueStmt;
            this.falseStmt = falseStmt;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIf(this);
        }
    }

    public static class Class extends Stmt {
        final Token name;
        final Expr.Variable superclass;
        final List<Stmt.Function> methods;
        final List<Stmt.Function> statics;
        public Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods, List<Stmt.Function> statics) {
            this.name = name;
            this.superclass = superclass;
            this.methods = methods;
            this.statics = statics;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitClass(this);
        }
    }

    public static class Var extends Stmt {
        final Token name;
        final Expr initializer;
        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVar(this);
        }
    }

    public static class Expression extends Stmt {
        final Expr expression;
        public Expression(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpression(this);
        }
    }

    public static class Break extends Stmt {
        final Token name;
        public Break(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBreak(this);
        }
    }

    public static class Continue extends Stmt {
        final Token name;
        public Continue(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitContinue(this);
        }
    }

    public static class Return extends Stmt {
        final Token name;
        final Expr value;
        public Return(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitReturn(this);
        }
    }

    public static class Function extends Stmt {
        final Token name;
        final List<Token> params;
        final List<Stmt> body;
        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitFunction(this);
        }
    }

    public static class Print extends Stmt {
        final Expr expression;
        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrint(this);
        }
    }

}
