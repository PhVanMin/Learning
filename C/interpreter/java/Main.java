import mpp.*;

public class Main {
    public static void main(String[] args) {
        Expr left = new Expr.Binary(
                new Expr.Literal(1), new Token(TokenType.PLUS, "+", null, 1),
                new Expr.Literal(2)

        );
        Expr right = new Expr.Binary(
                new Expr.Literal(3), new Token(TokenType.MINUS, "-", null, 1),
                new Expr.Literal(4));
        Expr binary = new Expr.Binary(left, new Token(TokenType.STAR, "*", null, 1), right);
    }
}
