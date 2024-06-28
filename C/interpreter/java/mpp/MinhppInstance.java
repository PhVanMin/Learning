package mpp;

import java.util.HashMap;
import java.util.Map;

public class MinhppInstance {
    protected Map<String, Object> fields;
    final MinhppClass mClass;

    MinhppInstance(MinhppClass mClass) {
        this.mClass = mClass;
        this.fields = new HashMap<>();
    }

    public void set(Token name, Object value) {
        fields.put(name.lexeme, value);
    }

    /* public String getName() {
        return mClass.name;
    } */

    public Object get(Token name) {
        if (fields.containsKey(name.lexeme))
            return fields.get(name.lexeme);

        if (mClass != null)
        { MinhppFunction method = mClass.findMethod(name.lexeme);
        if (method != null)
            return method.bind(this); }

        throw new RuntimeError(name, "Undefined property " + name.lexeme + ".");
    }

    @Override
    public String toString() {
        return mClass.name + " instance";
    }
}
