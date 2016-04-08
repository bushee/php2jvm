package pl.bushee.php2jvm;

import pl.bushee.php2jvm.function.PhpFunction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static pl.bushee.php2jvm.FunctionDefinition.FunctionType.INTERNAL;
import static pl.bushee.php2jvm.FunctionDefinition.FunctionType.USER;

@PhpInternal
public class Globals extends Context {

    private final Map<String, FunctionDefinition> functions;

    public Globals() {
        this(false);
    }

    public Globals(boolean useThreadSafeMap) {
        if (useThreadSafeMap) {
            functions = new Hashtable<>();
        } else {
            functions = new HashMap<>();
        }
    }

    public void registerFunctions(Object functionHolder) {
        for (Method method : functionHolder.getClass().getDeclaredMethods()) {
            PhpFunction phpFunction = method.getAnnotation(PhpFunction.class);
            if (phpFunction != null) {
                registerFunction(functionHolder, method, phpFunction);
            }
        }
    }

    private void registerFunction(Object functionHolder, Method method, PhpFunction functionMetadata) {
        if (functions.containsKey(functionMetadata.value())) {
            throw new FatalError("Cannot redeclare " + functionMetadata.value() + "()");
        }

        functions.put(functionMetadata.value(), createFunctionDefinition(functionHolder, method));
    }

    private FunctionDefinition createFunctionDefinition(Object functionHolder, Method method) {
        FunctionDefinition.FunctionType functionType = isInternalFunctionHolder(functionHolder) ? INTERNAL : USER;
        return new FunctionDefinition(functionHolder, method, functionType);
    }

    private boolean isInternalFunctionHolder(Object functionHolder) {
        return functionHolder.getClass().isAnnotationPresent(PhpInternal.class);
    }

    public Object callFunction(String functionName, Object... arguments) throws InvocationTargetException {
        FunctionDefinition function = functions.get(functionName);
        if (function == null) {
            throw new FatalError("Call to undefined function " + functionName + "()");
        }
        try {
            return function.call(arguments);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @PhpFunction("get_defined_functions")
    public PhpArray getDefinedFunctions() {
        PhpArray<String> internalFunctionsList = new PhpArray<>();
        PhpArray<String> userFunctionsList = new PhpArray<>();

        functions.forEach((name, function) -> {
            if (function.getType() == INTERNAL) {
                internalFunctionsList.append(name);
            } else {
                userFunctionsList.append(name);
            }
        });

        PhpArray<PhpArray<String>> result = new PhpArray<>();
        result.put("internal", internalFunctionsList);
        result.put("user", userFunctionsList);
        return result;
    }
}
