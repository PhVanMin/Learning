package mpp;

import java.util.List;

public abstract class Expr {
    interface Visitor<T> {
        T visitBinary(Binary expr);
        T visitTernary(Ternary expr);
        T visitGrouping(Grouping expr);
        T visitLiteral(Literal expr);
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
