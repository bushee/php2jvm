package pl.bushee.php2jvm;

import pl.bushee.php2jvm.function.OptionalBooleanArgument;
import pl.bushee.php2jvm.function.OptionalBooleanArrayArgument;
import pl.bushee.php2jvm.function.OptionalFloatArgument;
import pl.bushee.php2jvm.function.OptionalFloatArrayArgument;
import pl.bushee.php2jvm.function.OptionalIntegerArgument;
import pl.bushee.php2jvm.function.OptionalIntegerArrayArgument;
import pl.bushee.php2jvm.function.OptionalNullArgument;
import pl.bushee.php2jvm.function.OptionalStringArgument;
import pl.bushee.php2jvm.function.OptionalStringArrayArgument;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class FunctionDefinition {

    private final Object functionHolder;
    private final Method method;
    private final FunctionType type;

    FunctionDefinition(Object functionHolder, Method method, FunctionType type) {
        this.functionHolder = functionHolder;
        this.method = method;
        this.type = type;
    }

    FunctionType getType() {
        return type;
    }

    Object call(Object... arguments) throws InvocationTargetException, IllegalAccessException {
        // TODO arguments conversion
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < args.length; ++i) {
            if (i < arguments.length) {
                args[i] = arguments[i];
            } else {
                args[i] = getDefaultValueForArgument(i);
            }
        }
        return method.invoke(functionHolder, args);
    }

    private Object getDefaultValueForArgument(int argumentNum) {
        Annotation[] annotations = method.getParameterAnnotations()[argumentNum];
        for (int i = 0; i < annotations.length; ++i) {
            Annotation annotation = annotations[i];
            if (annotation instanceof OptionalBooleanArgument) {
                return ((OptionalBooleanArgument) annotation).value();
            }
            if (annotation instanceof OptionalBooleanArrayArgument) {
                return ((OptionalBooleanArrayArgument) annotation).value();
            }
            if (annotation instanceof OptionalFloatArgument) {
                return ((OptionalFloatArgument) annotation).value();
            }
            if (annotation instanceof OptionalFloatArrayArgument) {
                return ((OptionalFloatArrayArgument) annotation).value();
            }
            if (annotation instanceof OptionalIntegerArgument) {
                return ((OptionalIntegerArgument) annotation).value();
            }
            if (annotation instanceof OptionalIntegerArrayArgument) {
                return ((OptionalIntegerArrayArgument) annotation).value();
            }
            if (annotation instanceof OptionalStringArgument) {
                return ((OptionalStringArgument) annotation).value();
            }
            if (annotation instanceof OptionalStringArrayArgument) {
                return ((OptionalStringArrayArgument) annotation).value();
            }
            if (annotation instanceof OptionalNullArgument) {
                return null;
            }
        }
        return null;
    }

    enum FunctionType {
        INTERNAL, USER;
    }
}
