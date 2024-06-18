package mpp;

import java.util.List;

public abstract class Expr {
    interface Visitor<T> {
        T visitBinary(Binary expr);
        T visitAssign(Assign expr);
        T visitTernary(Ternary expr);
        T visitLogical(Logical expr);
        T visitLambda(Lambda expr);
        T visitGrouping(Grouping expr);
        T visitLiteral(Literal expr);
        T visitCall(Call expr);
        T visitVariable(Variable expr);
        T visitUnary(Unary expr);
    }

    abstract <T> T accept(Visitor<T> visitor);

    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;
        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinary(this);
        }
    }

    public static class Assign extends Expr {
        final Token name;
        final Expr value;
        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssign(this);
        }
    }

    public static class Ternary extends Expr {
        final Expr condition;
        final Expr trueExpr;
        final Expr falseExpr;
        public Ternary(Expr condition, Expr trueExpr, Expr falseExpr) {
            this.condition = condition;
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitTernary(this);
        }
    }

    public static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;
        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogical(this);
        }
    }

    public static class Lambda extends Expr {
        final Stmt.Function function;
        public Lambda(Stmt.Function function) {
            this.function = function;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLambda(this);
        }
    }

    public static class Grouping extends Expr {
        final Expr expression;
        public Grouping(Expr expression) {
            this.expression = expression;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGrouping(this);
        }
    }

    public static class Literal extends Expr {
        final Object value;
        public Literal(Object value) {
            this.value = value;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteral(this);
        }
    }

    public static class Call extends Expr {
        final Expr callee;
        final Token paren;
        final List<Expr> arguments;
        public Call(Expr callee, Token paren, List<Expr> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitCall(this);
        }
    }

    public static class Variable extends Expr {
        final Token name;
        public Variable(Token name) {
            this.name = name;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariable(this);
        }
    }

    public static class Unary extends Expr {
        final Token operator;
        final Expr right;
        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnary(this);
        }
    }

}
