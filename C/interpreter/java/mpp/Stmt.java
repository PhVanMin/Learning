package mpp;

import java.util.List;

public abstract class Stmt {
    interface Visitor<T> {
        T visitBlock(Block stmt);
        T visitWhile(While stmt);
        T visitIf(If stmt);
        T visitVar(Var stmt);
        T visitExpression(Expression stmt);
        T visitPrint(Print stmt);
    }

    abstract <T> T accept(Visitor<T> visitor);

    public static class Block extends Stmt {
        final List<Stmt> statements;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBlock(this);
        }

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }
    }

    public static class While extends Stmt {
        final Expr condition;
        final Stmt whileStmt;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitWhile(this);
        }

        public While(Expr condition, Stmt whileStmt) {
            this.condition = condition;
            this.whileStmt = whileStmt;
        }
    }

    public static class If extends Stmt {
        final Expr condition;
        final Stmt trueStmt;
        final Stmt falseStmt;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitIf(this);
        }

        public If(Expr condition, Stmt trueStmt, Stmt falseStmt) {
            this.condition = condition;
            this.trueStmt = trueStmt;
            this.falseStmt = falseStmt;
        }
    }

    public static class Var extends Stmt {
        final Token name;
        final Expr initializer;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVar(this);
        }

        public Var(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }
    }

    public static class Expression extends Stmt {
        final Expr expression;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitExpression(this);
        }

        public Expression(Expr expression) {
            this.expression = expression;
        }
    }

    public static class Print extends Stmt {
        final Expr expression;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitPrint(this);
        }

        public Print(Expr expression) {
            this.expression = expression;
        }
    }

}
