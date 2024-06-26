package mpp;

import java.util.List;
import java.util.Map;

class MinhppClass extends MinhppInstance implements MinhppCallable {
    final String name;
    final MinhppClass superclass;
    private final Map<String, MinhppFunction> methods;

    MinhppClass(MinhppClass metaclass, MinhppClass superclass, String name,
            Map<String, MinhppFunction> methods) {
        super(metaclass);
        this.name = name;
        this.superclass = superclass;
        this.methods = methods;
    }

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't assign class property.");
    }

    public MinhppFunction findMethod(String name) {
        if (methods.containsKey(name))
            return methods.get(name);

        if (superclass != null)
            return superclass.findMethod(name);

        return null;
    }

    @Override
    public String toString() {
        return "class " + name;
    }

    @Override
    public int arity() {
        MinhppFunction initializer = findMethod(name);
        if (initializer != null)
            return initializer.arity();
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
