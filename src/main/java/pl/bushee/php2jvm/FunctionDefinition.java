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
import pl.bushee.php2jvm.function.PhpFunction;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

abstract class FunctionDefinition {
    protected final Method method;
    private Object[] defaultValues;

    FunctionDefinition(Method method) {
        this.method = method;
        assertAnnotatedProperly(method);
    }

    private void assertAnnotatedProperly(Method method) {
        if (!method.isAnnotationPresent(PhpFunction.class)) {
            throw new IllegalArgumentException(
                String.format(
                    "Only a method annotated with @PhpFunction may be registered as a function (%s::%s() was given).",
                    method.getDeclaringClass().getName(),
                    method.getName()
                )
            );
        }
    }

    public final String getName() {
        return method.getAnnotation(PhpFunction.class).value();
    }

    public final boolean isInternal() {
        return method.getDeclaringClass().isAnnotationPresent(PhpInternal.class);
    }

    public final FunctionType getType() {
        return isInternal() ? FunctionType.INTERNAL : FunctionType.USER;
    }

    public abstract Object call(Object... arguments) throws IllegalAccessException;

    protected Object[] prepareArguments(Object[] arguments) {
        // TODO arguments conversion
        Object[] args = new Object[method.getParameterCount()];
        for (int i = 0; i < args.length; ++i) {
            if (i < arguments.length) {
                args[i] = arguments[i];
            } else {
                args[i] = getDefaultArgumentValue(i);
            }
        }
        return args;
    }

    protected Object getDefaultArgumentValue(int argumentIndex) {
        if (defaultValues == null) {
            defaultValues = createDefaultValues();
        }
        return defaultValues[argumentIndex];
    }

    private Object[] createDefaultValues() {
        final int parametersCount = method.getParameterCount();
        final Object[] defaultValues = new Object[parametersCount];
        final Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parametersCount; ++i) {
            defaultValues[i] = getDefaultValue(parameterAnnotations[i]);
        }
        return defaultValues;
    }

    private Object getDefaultValue(Annotation[] annotations) {
        for (Annotation annotation : annotations) {
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

    public enum FunctionType {
        INTERNAL, USER;
    }
}
