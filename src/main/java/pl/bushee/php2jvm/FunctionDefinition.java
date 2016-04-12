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
import java.lang.reflect.Method;

abstract class FunctionDefinition {
    protected final Method method;
    protected final String name;

    FunctionDefinition(Method method, String name) {
        this.method = method;
        this.name = name;
    }

    public final String getName() {
        return name;
    }

    public final boolean isInternal() {
        return method.getDeclaringClass().isAnnotationPresent(PhpInternal.class);
    }

    public final FunctionType getType() {
        return isInternal() ? FunctionType.INTERNAL : FunctionType.USER;
    }

    public abstract Object call(Object... arguments) throws IllegalAccessException;

    private Object getDefaultValueForArgument(int argumentNum) {
        // TODO optimize, eg. create list of defaults once per function
        for (Annotation annotation : method.getParameterAnnotations()[argumentNum]) {
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

    protected Object[] prepareArguments(Object[] arguments) {
        // TODO arguments conversion
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < args.length; ++i) {
            if (i < arguments.length) {
                args[i] = arguments[i];
            } else {
                args[i] = getDefaultValueForArgument(i);
            }
        }
        return args;
    }

    public enum FunctionType {
        INTERNAL, USER;
    }
}
