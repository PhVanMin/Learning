package mpp;

import java.util.List;

public abstract class Expr {
    interface Visitor<T> {
        T visitBinary(Binary expr);
        T visitAssign(Assign expr);
        T visitTernary(Ternary expr);
        T visitLogical(Logical expr);
        T visitGrouping(Grouping expr);
        T visitLiteral(Literal expr);
        T visitVariable(Variable expr);
        T visitUnary(Unary expr);
    }

    abstract <T> T accept(Visitor<T> visitor);

    public static class Binary extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitBinary(this);
        }

        public Binary(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Assign extends Expr {
        final Token name;
        final Expr value;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitAssign(this);
        }

        public Assign(Token name, Expr value) {
            this.name = name;
            this.value = value;
        }
    }

    public static class Ternary extends Expr {
        final Expr condition;
        final Expr trueExpr;
        final Expr falseExpr;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitTernary(this);
        }

        public Ternary(Expr condition, Expr trueExpr, Expr falseExpr) {
            this.condition = condition;
            this.trueExpr = trueExpr;
            this.falseExpr = falseExpr;
        }
    }

    public static class Logical extends Expr {
        final Expr left;
        final Token operator;
        final Expr right;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLogical(this);
        }

        public Logical(Expr left, Token operator, Expr right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
    }

    public static class Grouping extends Expr {
        final Expr expression;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitGrouping(this);
        }

        public Grouping(Expr expression) {
            this.expression = expression;
        }
    }

    public static class Literal extends Expr {
        final Object value;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitLiteral(this);
        }

        public Literal(Object value) {
            this.value = value;
        }
    }

    public static class Variable extends Expr {
        final Token name;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitVariable(this);
        }

        public Variable(Token name) {
            this.name = name;
        }
    }

    public static class Unary extends Expr {
        final Token operator;
        final Expr right;

        @Override
        <T> T accept(Visitor<T> visitor) {
            return visitor.visitUnary(this);
        }

        public Unary(Token operator, Expr right) {
            this.operator = operator;
            this.right = right;
        }
    }

}
