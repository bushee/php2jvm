package pl.bushee.php2jvm;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

class OverloadedFunctionDefinition implements FunctionDefinition {
    private final Object functionHolder;
    private final List<Method> methods;
    private final FunctionType type;

    OverloadedFunctionDefinition(Object functionHolder, Method method, FunctionType type) {
        this.functionHolder = functionHolder;
        this.type = type;
        methods = new ArrayList<>();
        methods.add(method);
    }

    public boolean addOverride(Object functionHolder, Method method) {
        if (this.functionHolder == functionHolder) {
            this.methods.add(method);
            return true;
        }
        return false;
    }

    @Override
    public FunctionType getType() {
        return type;
    }

    @Override
    public Object call(Object... arguments) {
        // TODO
        throw new UnsupportedOperationException();
    }
}
