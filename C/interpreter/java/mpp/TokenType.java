package mpp;

public enum TokenType {
    LEFT_BRAC, RIGHT_BRAC, LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, PERCEN,
    COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR, QUESTION, COLON,

    BANG, BANG_EQUAL, PLUS_PLUS, MINUS_MINUS,
    EQUAL, EQUAL_EQUAL,
    GREATER, GREATER_EQUAL,
    LESS, LESS_EQUAL,

    IDENTIFIER, STRING, NUMBER, STATIC,

    AND, CLASS, ELSE, FALSE, TRUE, FUN, FOR, IF, NIL, OR,
    RETURN, SUPER, THIS, VAR, WHILE, BREAK, CONTINUE,

    EOF
}
