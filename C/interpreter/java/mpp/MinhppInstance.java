package mpp;

import java.util.HashMap;
import java.util.Map;

public class MinhppInstance {
    private Map<String, Object> fields;
    private MinhppClass mClass;

    MinhppInstance(MinhppClass mClass) {
        this.mClass = mClass;
        this.fields = new HashMap<>();
    }

    public void set(Token name, Object value) {
        MinhppFunction method = mClass.findMethod(name.lexeme);
        if (method == null)
            fields.put(name.lexeme, value);

        throw new RuntimeError(name, "Can't assign class method.");
    }

    public String getName() {
        return mClass.name;
    }

    public Object get(Token name) {
        MinhppFunction method = mClass.findMethod(name.lexeme);
        if (method != null)
            return method.bind(this);

        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);

        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    @Override
    public String toString() {
        return mClass.name + " instance";
    }
}
