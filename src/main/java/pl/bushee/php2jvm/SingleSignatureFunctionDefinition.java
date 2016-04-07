package pl.bushee.php2jvm;

import java.lang.reflect.Method;

class SingleSignatureFunctionDefinition implements FunctionDefinition {

    private final Object functionHolder;
    private final Method method;
    private final FunctionType type;

    SingleSignatureFunctionDefinition(Object functionHolder, Method method, FunctionType type) {
        this.functionHolder = functionHolder;
        this.method = method;
        this.type = type;
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
