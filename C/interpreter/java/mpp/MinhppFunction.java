package mpp;

import java.util.List;

public class MinhppFunction implements MinhppCallable {
    private final Stmt.Function func;
    private final Environment closure;
    private final boolean isInitializer;

    public MinhppFunction(Stmt.Function function, Environment closure, boolean isInitializer) {
        func = function;
        this.isInitializer = isInitializer;
        this.closure = closure;
    }

    public MinhppFunction bind(MinhppInstance mInstance) {
        Environment environment = new Environment(closure);
        environment.define("this", mInstance);
        return new MinhppFunction(func, environment, isInitializer);
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
        return "<fn " + func.name.lexeme + ">";
    }
}
