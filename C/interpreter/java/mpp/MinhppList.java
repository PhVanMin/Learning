package mpp;

import java.util.ArrayList;
import java.util.List;

/**
 * MinhppList
 */
public class MinhppList extends MinhppInstance {
    private List<Object> items = new ArrayList<>();

    @Override
    public void set(Token name, Object value) {
        throw new RuntimeError(name, "Can't set list property.");
    }

    MinhppList(List<Object> args) {
        super(null);

        for (Object arg : args)
            items.add(arg);

        super.fields.put("size", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return items.size();
            }
        });

        super.fields.put("remove", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return items.remove(args.get(0));
            }
        });

        super.fields.put("get", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                return items.get(0);
            }
        });

        super.fields.put("add", new MinhppCallable() {
            @Override
            public int arity() {
                return 1;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> args) {
                items.add(args.get(0));
                return args.get(0);
            }
        });
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (Object item : items) {
            builder.append(item.toString() + ", ");
        }
        builder.delete(builder.length() - 2, builder.length());
        builder.append(']');

        return builder.toString();
    }
}
