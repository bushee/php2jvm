package pl.bushee.php2jvm;

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
        FunctionDefinition alreadyRegisteredFunction = functions.get(functionMetadata.value());
        if (alreadyRegisteredFunction != null) {
            if (!tryToOverrideFunction(alreadyRegisteredFunction, functionHolder, method, functionMetadata)) {
                throw new FatalError("Cannot redeclare " + functionMetadata.value() + "()");
            }
        }

        functions.put(functionMetadata.value(), createFunctionDefinition(functionHolder, method, functionMetadata));
    }

    private boolean tryToOverrideFunction(FunctionDefinition alreadyRegisteredFunction, Object functionHolder, Method method, PhpFunction functionMetadata) {
        if (alreadyRegisteredFunction instanceof OverloadedFunctionDefinition && functionMetadata.overloaded()) {
            OverloadedFunctionDefinition overloadedFunctionDefinition = (OverloadedFunctionDefinition) alreadyRegisteredFunction;
            return overloadedFunctionDefinition.addOverride(functionHolder, method);
        }
        return false;
    }

    private FunctionDefinition createFunctionDefinition(Object functionHolder, Method method, PhpFunction functionMetadata) {
        FunctionDefinition.FunctionType functionType = isInternalFunctionHolder(functionHolder) ? INTERNAL : USER;
        if (functionMetadata.overloaded()) {
            return new OverloadedFunctionDefinition(functionHolder, method, functionType);
        } else {
            return new SingleSignatureFunctionDefinition(functionHolder, method, functionType);
        }
    }

    private boolean isInternalFunctionHolder(Object functionHolder) {
        return functionHolder.getClass().isAnnotationPresent(PhpInternal.class);
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
