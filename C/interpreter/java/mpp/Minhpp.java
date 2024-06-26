package mpp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Minhpp {
    private static boolean hadError = false;
    private static boolean hadRuntimeError = false;
    private static Interpreter interpreter = new Interpreter();

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jmpp [script]");
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()), false);

        if (hadError)
            System.exit(65);

        if (hadRuntimeError)
            System.exit(70);
    }

    private static void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (;;) {
            System.out.print("> ");
            String line = reader.readLine();
            if (line == null)
                break;
            run(line, true);
            hadError = false;
        }
    }

    private static void run(String source, boolean cmd) {
        Scanner scanner = new Scanner(source);
        List<Token> tokens = scanner.scanTokens();
        if (cmd)
            tokens.add(tokens.size() - 1, new Token(TokenType.SEMICOLON, ";", null, 1));
        Parser parser = new Parser(tokens);
        List<Stmt> statements = parser.parse();

        if (hadError)
            return;

        Resolver resolver = new Resolver(interpreter);
        resolver.resolve(statements);

        if (hadError)
            return;

        interpreter.interpret(statements, cmd);
    }

    public static void warning(Token token, String message) {
        System.err.println("[line " + token.line + "] Warning: " + message);
    }

    public static void runtimeError(RuntimeError error) {
        System.err.println(error.getMessage() + " [line " + error.token.line + "]");
        hadRuntimeError = true;
    }

    public static void error(Token token, String message) {
        if (token.type == TokenType.EOF)
            report(token.line, " at end", message);
        else
            report(token.line, " at '" + token.lexeme + "'", message);
    }

    public static void error(int line, String message) {
        report(line, "", message);
    }

    private static void report(int line, String where, String message) {
        System.err.println("[line " + line + "] Error" + where + ": " + message);
        hadError = true;
    }
}
