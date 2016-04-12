package pl.bushee.php2jvm;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

// TODO UT
class StaticFunctionDefinition extends FunctionDefinition {

    StaticFunctionDefinition(Method method) {
        super(method);
        assertStatic(method);
    }

    private void assertStatic(Method method) {
        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalArgumentException(
                String.format(
                    "Static method was expected (trying to declare %s::%s() as %s()).",
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
            return method.invoke(null, prepareArguments(arguments));
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Something went terribly wrong...", e);
        }
    }
}
