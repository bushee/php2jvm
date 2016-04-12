package pl.bushee.php2jvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// TODO UT
public class InstanceFunctionDefinition extends FunctionDefinition {
    private final Object functionHolder;

    public InstanceFunctionDefinition(Object functionHolder, Method method) {
        super(method);
        assertNotStatic(method);
        this.functionHolder = functionHolder;
    }

    private void assertNotStatic(Method method) {
        if (Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException(
                String.format(
                    "Non static method was expected (trying to declare %s::%s() as %s()).",
                    method.getDeclaringClass().getName(),
                    method.getName(),
                    getName()
                )
            );
        }
    }

    @Override
    public Object call(Object... arguments) throws IllegalAccessException {
        try {
            return method.invoke(functionHolder, prepareArguments(arguments));
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Something went terribly wrong...", e);
        }
    }
}
