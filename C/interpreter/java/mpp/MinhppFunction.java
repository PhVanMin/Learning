package mpp;

import java.util.List;

public class MinhppFunction implements MinhppCallable {
    private final Stmt.Function func;
    private final Environment closure;

    public MinhppFunction(Stmt.Function function, Environment closure) {
        func = function;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return func.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < func.params.size(); ++i) {
            environment.define(func.params.get(i).lexeme, args.get(i));
        }

        try {
            interpreter.executeBlock(func.body, environment);
        } catch (Return returnValue) {
            return returnValue.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + func.name.lexeme + ">";
    }
}
