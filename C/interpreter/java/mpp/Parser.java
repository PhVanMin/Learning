package mpp;

import static mpp.TokenType.*;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            return null;
        }
    }

    private Expr expression() {
        if (match(EQUAL_EQUAL)) {
            System.out.println("No left-hand operand.");
            synchronizeError();
        }

        return ternary();
    }

    private Expr ternary() {
        Expr expr = comma();

        if (match(QUESTION)) {
            Expr trueExpr = ternary();
            consume(COLON, "Expect false expression.");
            Expr falseExpr = comma();

            return new Expr.Ternary(expr, trueExpr, falseExpr);
        }

        return expr;
    }

    private Expr comma() {
        Expr expr = equality();

        while (match(COMMA)) {
            Token operator = peek(-1);
            Expr right = comma();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr equality() {
        Expr expr = comparision();

        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
            Token operator = peek(-1);
            Expr right = comparision();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparision() {
        Expr expr = term();

        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = peek(-1);
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();
        while (match(MINUS, PLUS)) {
            Token operator = peek(-1);
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
        while (match(SLASH, STAR)) {
            Token operator = peek(-1);
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS)) {
            Token operator = peek(-1);
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (match(TRUE))
            return new Expr.Literal(true);
        if (match(FALSE))
            return new Expr.Literal(false);
        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, TokenType.STRING)) {
            return new Expr.Literal(peek(-1).literal);
        }

        if (match(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(0), "Unidentified primary.");
    }

    private Token consume(TokenType type, String errMsg) {
        if (check(type)) {
            return advance();
        }

        throw error(peek(0), errMsg);
    }

    private ParseError error(Token token, String msg) {
        Minhpp.error(token, msg);
        return new ParseError();
    }

    private void synchronizeError() {
        advance();

        while (!isAtEnd()) {
            if (peek(-1).type == SEMICOLON)
                return;

            switch (peek(0).type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
                default:
                    advance();
            }
        }
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private Token advance() {
        if (!isAtEnd())
            current++;
        return peek(-1);
    }

    private boolean check(TokenType type) {
        if (isAtEnd())
            return false;
        return peek(0).type == type;
    }

    private boolean isAtEnd() {
        return peek(0).type == EOF;
    }

    private Token peek(int index) {
        return tokens.get(current + index);
    }
}
