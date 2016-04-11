package pl.bushee.php2jvm;

import org.reflections.Reflections;
import pl.bushee.php2jvm.function.PhpFunction;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import static pl.bushee.php2jvm.FunctionDefinition.FunctionType.INTERNAL;

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

    public void registerFunctions(Class functionHolderClass) {
        for (Method method : functionHolderClass.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            PhpFunction phpFunction = method.getAnnotation(PhpFunction.class);
            if (phpFunction != null) {
                registerFunction(new StaticFunctionDefinition(method, phpFunction.value()));
            }
        }
    }

    public void registerFunctions(Object functionHolderObject) {
        for (Method method : functionHolderObject.getClass().getDeclaredMethods()) {
            if (Modifier.isStatic(method.getModifiers())) {
                continue;
            }
            PhpFunction phpFunction = method.getAnnotation(PhpFunction.class);
            if (phpFunction != null) {
                registerFunction(new InstanceFunctionDefinition(functionHolderObject, method, phpFunction.value()));
            }
        }
    }

    private void registerFunction(FunctionDefinition functionDefinition) {
        if (functions.containsKey(functionDefinition.getName())) {
            throw new FatalError(String.format("Cannot redeclare %s()", functionDefinition.getName()));
        }

        functions.put(functionDefinition.getName(), functionDefinition);
    }

    public void registerInternalFunctions() {
        new Reflections("pl.bushee.php2jvm")
            .getTypesAnnotatedWith(PhpInternal.class)
            .forEach(this::registerFunctions);
        registerFunctions(this);
    }

    public Object callFunction(String functionName, Object... arguments) {
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
    public PhpArray<PhpArray<String>> getDefinedFunctions() {
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
