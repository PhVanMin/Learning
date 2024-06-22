package mpp;

import java.util.HashMap;
import java.util.Map;

public class MinhppInstance {
    private Map<String, Object> fields = new HashMap<>();
    private MinhppClass mClass;

    MinhppInstance(MinhppClass mClass) {
        this.mClass = mClass;
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);

        MinhppFunction method = mClass.findMethod(name.lexeme);
        if (method != null)
            return method.bind(this);

        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    @Override
    public String toString() {
        return mClass.name + " instance";
    }
}
