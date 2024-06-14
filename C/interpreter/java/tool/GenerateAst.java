package tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

public class GenerateAst {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(64);
        }

        String outputDir = args[0];
        defineAst(outputDir, "Expr", Arrays.asList(
                "Binary : Expr left, Token operator, Expr right",
                "Assign : Token name, Expr value",
                "Ternary : Expr condition, Expr trueExpr, Expr falseExpr",
                "Logical : Expr left, Token operator, Expr right",
                "Grouping : Expr expression",
                "Literal : Object value",
                "Variable : Token name",
                "Unary : Token operator, Expr right"));

        defineAst(outputDir, "Stmt", Arrays.asList(
                "Block : List<Stmt> statements",
                "While : Expr condition, Stmt whileStmt",
                "If : Expr condition, Stmt trueStmt, Stmt falseStmt",
                "Var : Token name, Expr initializer",
                "Expression : Expr expression",
                "Print : Expr expression"));
    }

    private static void defineAst(String outputDir, String name, List<String> types) throws IOException {
        String path = outputDir + "/" + name + ".java";
        PrintWriter writer = new PrintWriter(path, "UTF-8");

        writer.println("package mpp;");
        writer.println();
        writer.println("import java.util.List;");
        writer.println();
        writer.println("public abstract class " + name + " {");

        defineVisitor(writer, name, types);

        for (String type : types) {
            String[] split = type.split(":");
            String className = split[0].trim();
            String fields = split[1].trim();
            defineType(writer, name, className, fields);
        }

        writer.println("}");
        writer.close();
    }

    private static void defineVisitor(PrintWriter writer, String name, List<String> types) {
        writer.println("    interface Visitor<T> {");
        for (String type : types) {
            String className = type.split(":")[0].trim();
            writer.println("        T visit" + className +
                    "(" + className + " " + name.toLowerCase() + ");");
        }
        writer.println("    }");

        writer.println();
        writer.println("    abstract <T> T accept(Visitor<T> visitor);");
        writer.println();
    }

    private static void defineType(PrintWriter writer, String name, String className, String fields) {
        writer.println("    public static class " + className + " extends " + name + " {");

        String[] fieldsSplit = fields.split(", ");
        for (String field : fieldsSplit) {
            writer.println("        final " + field + ";");
        }

        writer.println();
        writer.println("        @Override");
        writer.println("        <T> T accept(Visitor<T> visitor) {");
        writer.println("            return visitor.visit" + className + "(this);");
        writer.println("        }");

        writer.println();
        writer.println("        public " + className + "(" + fields + ") {");
        for (String field : fieldsSplit) {
            String fieldName = field.split(" ")[1];
            writer.println("            this." + fieldName + " = " + fieldName + ";");
        }
        writer.println("        }");

        writer.println("    }");
        writer.println();
    }
}
