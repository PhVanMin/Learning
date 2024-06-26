package mpp;

import static mpp.TokenType.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {
    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    public List<Stmt> parse() {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Stmt declaration() {
        try {
            if (match(VAR))
                return varDeclaration();

            if (match(CLASS)) {
                return classDeclaration();
            }

            if (match(FUN))
                return function("function");

            return statement();
        } catch (ParseError error) {
            synchronizeError();
            return null;
        }
    }

    private Stmt classDeclaration() {
        Token name = consume(IDENTIFIER, "Expect class name.");

        Expr.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "Expect superclass name.");
            superclass = new Expr.Variable(peek(-1));
        }

        consume(LEFT_BRACE, "Expect '{' before class body.");
        List<Stmt.Function> methods = new ArrayList<>();
        List<Stmt.Function> statics = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            if (match(STATIC))
                statics.add(function("method"));
            else
                methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "Expect '}' after class body");
        return new Stmt.Class(name, superclass, methods, statics);
    }

    private Stmt.Function function(String type) {
        Token name = null;
        List<Token> parameters = null;

        if (check(IDENTIFIER) || !type.equals("lambda"))
            name = consume(IDENTIFIER, "Expect " + type + " name.");

        if (!type.equals("method") || check(LEFT_PAREN)) {
            parameters = new ArrayList<>();
            consume(LEFT_PAREN, "Expect '(' after " + type + " name.");
            if (!check(RIGHT_PAREN)) {
                do {
                    if (parameters.size() == 255)
                        error(peek(0), "Can't have more than 255 parameters.");

                    parameters.add(consume(IDENTIFIER, "Expect parameter name."));
                } while (match(COMMA));
            }
            consume(RIGHT_PAREN, "Expect ')' after parameters.");
        }

        consume(LEFT_BRACE, "Expect '{' before " + type + " body.");
        List<Stmt> body = block();
        return new Stmt.Function(name, parameters, body);
    }

    private Stmt.Var varDeclaration() {
        Token name = consume(IDENTIFIER, "Expect variable name.");

        Expr initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration");
        return new Stmt.Var(name, initializer);
    }

    private Stmt statement() {
        if (match(IF))
            return ifStatement();

        if (match(BREAK))
            return breakStatement();

        if (match(CONTINUE))
            return continueStatement();

        if (match(RETURN))
            return returnStatement();

        if (match(WHILE))
            return whileStatement();

        if (match(FOR))
            return forStatement();

        if (match(LEFT_BRACE))
            return new Stmt.Block(block());

        return expressionStatement();
    }

    private Stmt returnStatement() {
        Token name = peek(-1);
        Expr value = null;

        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "Expect ';' after return value.");
        return new Stmt.Return(name, value);
    }

    private Stmt breakStatement() {
        consume(SEMICOLON, "Expect ';' after break.");
        return new Stmt.Break(peek(-2));
    }

    private Stmt continueStatement() {
        consume(SEMICOLON, "Expect ';' after break.");
        return new Stmt.Continue(peek(-2));
    }

    private Stmt forStatement() {
        consume(LEFT_PAREN, "Expected '(' after for.");
        Stmt ini = null;
        if (match(SEMICOLON)) {
            ini = null;
        } else if (match(VAR)) {
            ini = varDeclaration();
        } else {
            ini = expressionStatement();
        }

        Expr condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "Expected ';' after loop condition.");

        Expr increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "Expected ')' after for statement.");

        Stmt body = statement();

        if (condition == null)
            condition = new Expr.Literal(true);

        if (increment != null)
            body = new Stmt.Loop(condition, body, new Stmt.Expression(increment));
        else
            body = new Stmt.Loop(condition, body, null);

        if (ini != null)
            body = new Stmt.Block(Arrays.asList(ini, body));
        return body;
    }

    private Stmt whileStatement() {
        consume(LEFT_PAREN, "Expected '(' after while.");
        Expr condition = expression();
        consume(RIGHT_PAREN, "Expected ')' after while condition.");

        Stmt whileStmt = statement();

        return new Stmt.Loop(condition, whileStmt, null);
    }

    private Stmt ifStatement() {
        consume(LEFT_PAREN, "Expected '(' after if condition.");
        Expr expr = expression();
        consume(RIGHT_PAREN, "Expected ')' after if condition.");

        Stmt trueStmt = statement();
        Stmt falseStmt = null;
        if (match(ELSE))
            falseStmt = statement();

        return new Stmt.If(expr, trueStmt, falseStmt);
    }

    private List<Stmt> block() {
        List<Stmt> statements = new ArrayList<>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expected '}' after block.");

        return statements;
    }

    private Stmt expressionStatement() {
        Expr expression = expression();
        consume(SEMICOLON, "Expect ';' after expression.");
        return new Stmt.Expression(expression);
    }

    private Expr expression() {
        return comma();
    }

    private Expr comma() {
        Expr expr = assignment();

        while (match(COMMA)) {
            Token operator = peek(-1);
            Expr right = comma();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr assignment() {
        Expr expr = ternary();

        if (match(EQUAL)) {
            Token equals = peek(-1);
            Expr value = expression();

            if (expr instanceof Expr.Variable) {
                Token name = ((Expr.Variable) expr).name;
                return new Expr.Assign(name, value);
            } else if (expr instanceof Expr.Get) {
                Expr.Get get = (Expr.Get) expr;
                return new Expr.Set(get.object, get.name, value);
            }

            throw error(equals, "Invalid assignment value.");
        }

        return expr;
    }

    private Expr ternary() {
        Expr expr = or();

        if (match(QUESTION)) {
            Expr trueExpr = expression();
            consume(COLON, "Expect false expression.");
            Expr falseExpr = or();

            return new Expr.Ternary(expr, trueExpr, falseExpr);
        }

        return expr;
    }

    private Expr or() {
        Expr left = and();

        while (match(OR)) {
            Token logicalToken = peek(-1);
            Expr right = and();
            left = new Expr.Logical(left, logicalToken, right);
        }

        return left;
    }

    private Expr and() {
        Expr left = equality();

        while (match(AND)) {
            Token logicToken = peek(-1);
            Expr right = equality();
            left = new Expr.Logical(left, logicToken, right);
        }

        return left;
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
        while (match(SLASH, STAR, PERCEN)) {
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

        return prefix();
    }

    private Expr prefix() {
        if (match(MINUS_MINUS) || match(PLUS_PLUS)) {
            Token operator = peek(-1);
            Expr right = call();
            if (right instanceof Expr.Get || right instanceof Expr.Variable)
                return new Expr.Unary(operator, right);

            throw error(operator, "Expect a variable after '" + operator.lexeme +"'.");
        }

        return postfix();
    }

    private Expr postfix() {
        Expr expr = call();

        if (match(MINUS_MINUS) || match(PLUS_PLUS)) {
            if (expr instanceof Expr.Get || expr instanceof Expr.Variable)
                return new Expr.Postfix(peek(-1), expr);

            throw error(peek(-1), "Expect a variable before '" + peek(-1).lexeme + "'.");
        }

        return expr;
    }

    private Expr call() {
        Expr calleeExpr = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                calleeExpr = finishCall(calleeExpr);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "Expect property name after '.'.");
                calleeExpr = new Expr.Get(calleeExpr, name);
            } else {
                break;
            }
        }

        return calleeExpr;
    }

    private Expr finishCall(Expr calleeExpr) {
        Token paren = peek(-1);
        List<Expr> arguments = new ArrayList<>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() == 255) {
                    error(peek(0), "Can't have more than 255 arguments.");
                }
                arguments.add(assignment());
            } while (match(COMMA));
        }

        consume(RIGHT_PAREN, "Expected ';' after arguments.");
        return new Expr.Call(calleeExpr, paren, arguments);
    }

    private Expr primary() {
        if (match(LEFT_BRAC)) {
            List<Expr> init = new ArrayList<>();
            if (!check(RIGHT_BRAC)) {
                do {
                    init.add(assignment());
                } while (match(COMMA));
            }
            consume(RIGHT_BRAC, "Expect ']' after list initialization.");
            return new Expr.MList(init);
        }

        if (match(THIS)) {
            return new Expr.This(peek(-1));
        }

        if (match(SUPER)) {
            Token keyword = peek(-1);
            consume(DOT, "Expect '.' after super.");
            Token method = consume(IDENTIFIER, "Expect superclass method name.");
            return new Expr.Super(keyword, method);
        }

        if (match(TRUE))
            return new Expr.Literal(true);

        if (match(FALSE))
            return new Expr.Literal(false);

        if (match(NIL))
            return new Expr.Literal(null);

        if (match(NUMBER, TokenType.STRING)) {
            return new Expr.Literal(peek(-1).literal);
        }

        if (match(FUN)) {
            return new Expr.Lambda(function("lambda"));
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(peek(-1));
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
