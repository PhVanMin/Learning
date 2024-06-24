package mpp;

import java.util.List;
import java.util.Map;

/**
 * MinhppClass
 */
class MinhppClass extends MinhppInstance implements MinhppCallable {
    final String name;
    private final Map<String, MinhppFunction> methods;

    MinhppClass(String name, Map<String, MinhppFunction> methods, Map<String, Object> statics) {
        super(null, statics);
        this.name = name;
        this.methods = methods;
    }

    public MinhppFunction findMethod(String name) {
        if (methods.containsKey(name))
            return methods.get(name);

        return null;
    }

    @Override
    public String toString() {
        return "class " + name;
    }

    @Override
    public int arity() {
        MinhppFunction initializer = findMethod(name); 
        if (initializer != null) return initializer.arity();
        return 0;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> args) {
        MinhppInstance instance = new MinhppInstance(this);

        MinhppFunction initializer = findMethod(name); 
        if (initializer != null)
            initializer.bind(instance).call(interpreter, args);

        return instance;
    }
}
