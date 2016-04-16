package pl.bushee.php2jvm

import pl.bushee.php2jvm.function.OptionalBooleanArgument
import pl.bushee.php2jvm.function.OptionalBooleanArrayArgument
import pl.bushee.php2jvm.function.OptionalFloatArgument
import pl.bushee.php2jvm.function.OptionalFloatArrayArgument
import pl.bushee.php2jvm.function.OptionalIntegerArgument
import pl.bushee.php2jvm.function.OptionalIntegerArrayArgument
import pl.bushee.php2jvm.function.OptionalNullArgument
import pl.bushee.php2jvm.function.OptionalStringArgument
import pl.bushee.php2jvm.function.OptionalStringArrayArgument
import pl.bushee.php2jvm.function.PhpFunction
import spock.lang.Specification
import spock.lang.Unroll

class GlobalsSpec extends Specification {

    def "registered internal function should be returned in defined functions list"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new InternalFunctionHolder())

        then:
        globals.definedFunctions['internal'].containsValue('some_function1')
    }

    def "registered user function should be returned in defined functions list"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new UserFunctionHolder())

        then:
        globals.definedFunctions['user'].containsValue('some_function2')
    }

    def "many registered functions should not interfere with each other"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new UserFunctionHolder())
        globals.registerFunctions(new InternalFunctionHolder())

        then:
        def definedFunctions = globals.definedFunctions
        definedFunctions['internal'].containsValue('some_function1')
        definedFunctions['user'].containsValue('some_function2')
    }

    def "registering static method as a function should work properly"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(StaticFunctionHolder)

        then:
        globals.definedFunctions['user'].containsValue('some_static_function')
        globals.definedFunctions['user'].containsValue('parameterless_static_function')
    }

    @Unroll
    def "trying to overwrite #original function with #conflicting function should raise an error"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(originalFunctionHolder)
        globals.registerFunctions(conflictingFunctionsHolder)

        then:
        thrown FatalError

        where:
        originalFunctionHolder       | conflictingFunctionsHolder
        new InternalFunctionHolder() | new ConflictingInternalFunctionHolder()
        new InternalFunctionHolder() | new ConflictingUserFunctionHolder()
        new UserFunctionHolder()     | new ConflictingInternalFunctionHolder()
        new UserFunctionHolder()     | new ConflictingUserFunctionHolder()

        original = originalFunctionHolder instanceof InternalFunctionHolder ? 'internal' : 'user'
        conflicting = conflictingFunctionsHolder instanceof ConflictingInternalFunctionHolder ? 'internal' : 'user'
    }

    def "calling function should delegate to actual annotated method (test for instance method)"() {
        given:
        def globals = new Globals()
        def functionHolder = new InternalFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        def result = globals.callFunction('some_function1')

        then:
        result == 'some function 1 result'
        functionHolder.sideEffect == 'some side effect'
    }

    def "calling function should delegate to actual annotated method (test for static method)"() {
        given:
        def globals = new Globals()
        globals.registerFunctions(StaticFunctionHolder)

        when:
        def result = globals.callFunction('parameterless_static_function')

        then:
        result == 'parameterless static function result'
        StaticFunctionHolder.sideEffect == 'some side effect'
    }

    def "calling function with parameters should delegate to actual annotated method (test for instance method)"() {
        given:
        def globals = new Globals()
        def functionHolder = new UserFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        def result = globals.callFunction('some_function2', 'a', 'b', 'c')

        then:
        result == 'concatenation result = "abc"'
        functionHolder.receivedA == 'a'
        functionHolder.receivedB == 'b'
        functionHolder.receivedC == 'c'
    }

    def "calling function with parameters should delegate to actual annotated method (test for static method)"() {
        given:
        def globals = new Globals()
        globals.registerFunctions(StaticFunctionHolder)

        when:
        def result = globals.callFunction('some_static_function', 'a', 'b', 'c')

        then:
        result == 'concatenation result = "abc"'
        StaticFunctionHolder.receivedA == 'a'
        StaticFunctionHolder.receivedB == 'b'
        StaticFunctionHolder.receivedC == 'c'
    }

    def "commanding to register all internal functions should result in registering proper functions"() {
        given:
        def globals = new Globals()

        when:
        globals.registerInternalFunctions()

        then:
        def internalFunctions = globals.definedFunctions['internal']
        internalFunctions.size() == 5
        // TODO extract expected function names somewhere?
        internalFunctions.containsValue('get_defined_functions')
        internalFunctions.containsValue('is_finite')
        internalFunctions.containsValue('is_infinite')
        internalFunctions.containsValue('is_nan')
        internalFunctions.containsValue('is_numeric')
    }

    @PhpInternal
    private static class InternalFunctionHolder {
        def sideEffect = null

        @PhpFunction('some_function1')
        def someFunction1() {
            sideEffect = 'some side effect'
            return 'some function 1 result'
        }
    }

    private static class UserFunctionHolder {
        def receivedA = null
        def receivedB = null
        def receivedC = null

        @PhpFunction('some_function2')
        def someFunction2(a, b, c) {
            receivedA = a
            receivedB = b
            receivedC = c
            return "concatenation result = \"$a$b$c\""
        }
    }

    @PhpInternal
    private static class ConflictingInternalFunctionHolder {
        @PhpFunction('some_function1')
        def someFunction1() {}

        @PhpFunction('some_function2')
        def someFunction2() {}
    }

    private static class ConflictingUserFunctionHolder {
        @PhpFunction('some_function1')
        def someFunction1() {}

        @PhpFunction('some_function2')
        def someFunction2() {}
    }

    private static class StaticFunctionHolder {
        def static receivedA = null
        def static receivedB = null
        def static receivedC = null
        def static sideEffect = null

        @PhpFunction('parameterless_static_function')
        def static parameterlessStaticFunction() {
            sideEffect = 'some side effect'
            return 'parameterless static function result'
        }

        @PhpFunction('some_static_function')
        def static someStaticFunction(a, b, c) {
            receivedA = a
            receivedB = b
            receivedC = c
            return "concatenation result = \"$a$b$c\""
        }
    }
}
