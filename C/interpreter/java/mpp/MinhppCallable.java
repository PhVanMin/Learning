package mpp;

import java.util.List;

public interface MinhppCallable {
    int arity();
    Object call(Interpreter interpreter, List<Object> args);
}
