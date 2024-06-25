package mpp;

import java.util.List;

public class MinhppFunction implements MinhppCallable {
    private final Stmt.Function function;
    private final Environment closure;
    private final boolean isInitializer;

    public MinhppFunction(Stmt.Function function, Environment closure, boolean isInitializer) {
        this.function = function;
        this.isInitializer = isInitializer;
        this.closure = closure;
    }

    public MinhppFunction bind(MinhppInstance mInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", mInstance);
        return new MinhppFunction(function, environment, isInitializer);
    }

    @Override
    public int arity() {
        if (function.params == null)
            return -1;
        return function.params.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        Environment environment = new Environment(closure);
        if (function.params != null) {
            for (int i = 0; i < function.params.size(); ++i) {
                environment.define(function.params.get(i).lexeme, args.get(i));
            }
        }

        try {
            interpreter.executeBlock(function.body, environment);
        } catch (Return returnValue) {
            if (isInitializer)
                return closure.getAt(0, "this");
            return returnValue.value;
        }

        if (isInitializer)
            return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + function.name.lexeme + ">";
    }
}
