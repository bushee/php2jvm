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

    // TODO move below 3 tests to StaticFunctionDefinition and InstanceFunctionDefinition tests
    def "calling function with too little parameters should result in passing nulls for missing ones without default values"() {
        given:
        def globals = new Globals()
        def functionHolder = new UserFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        def result = globals.callFunction('some_function2', 'a')

        then:
        result == 'concatenation result = "anullnull"'
        functionHolder.receivedA == 'a'
        functionHolder.receivedB == null
        functionHolder.receivedC == null
    }

    def "calling function with too much parameters should result in ignoring the extra ones"() {
        given:
        def globals = new Globals()
        def functionHolder = new UserFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        def result = globals.callFunction('some_function2', 'a', 'b', 'c', 'd', 'e')

        then:
        result == 'concatenation result = "abc"'
        functionHolder.receivedA == 'a'
        functionHolder.receivedB == 'b'
        functionHolder.receivedC == 'c'
    }

    @Unroll
    def "calling function with missing parameters should result in using default values for them (passing #argsCount out of 9 arguments)"() {
        given:
        def globals = new Globals()
        def functionHolder = new DefaultValuesFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        globals.callFunction('default_values_function', arguments.toArray())

        then:
        functionHolder.receivedA == expectedA
        functionHolder.receivedB == expectedB
        functionHolder.receivedC == expectedC
        functionHolder.receivedD == expectedD
        functionHolder.receivedE == expectedE
        functionHolder.receivedF == expectedF
        functionHolder.receivedG == expectedG
        functionHolder.receivedH == expectedH
        functionHolder.receivedI == expectedI

        where:
        arguments                                     | expectedA | expectedB | expectedC | expectedD | expectedE | expectedF     | expectedG | expectedH    | expectedI
        []                                            | null      | true      | 3         | 1.2f      | 'abc'     | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a']                                         | 'a'       | true      | 3         | 1.2f      | 'abc'     | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b']                                    | 'a'       | 'b'       | 3         | 1.2f      | 'abc'     | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c']                               | 'a'       | 'b'       | 'c'       | 1.2f      | 'abc'     | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c', 'd']                          | 'a'       | 'b'       | 'c'       | 'd'       | 'abc'     | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c', 'd', 'e']                     | 'a'       | 'b'       | 'c'       | 'd'       | 'e'       | [true, false] | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c', 'd', 'e', 'f']                | 'a'       | 'b'       | 'c'       | 'd'       | 'e'       | 'f'           | [1, 2, 3] | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c', 'd', 'e', 'f', 'g']           | 'a'       | 'b'       | 'c'       | 'd'       | 'e'       | 'f'           | 'g'       | [2.2f, 3.3f] | ['a', 'b']
        ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h']      | 'a'       | 'b'       | 'c'       | 'd'       | 'e'       | 'f'           | 'g'       | 'h'          | ['a', 'b']
        ['a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i'] | 'a'       | 'b'       | 'c'       | 'd'       | 'e'       | 'f'           | 'g'       | 'h'          | 'i'

        argsCount = arguments.size()
    }

    def "commanding to register all internal functions should result in registering proper functions"() {
        given:
        def globals = new Globals();

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

    private static class DefaultValuesFunctionHolder {
        def receivedA, receivedB, receivedC, receivedD, receivedE, receivedF, receivedG, receivedH, receivedI

        @PhpFunction('default_values_function')
        def someFunction(
            @OptionalNullArgument a,
            @OptionalBooleanArgument(true) b, @OptionalIntegerArgument(3) c,
            @OptionalFloatArgument(1.2f) d, @OptionalStringArgument('abc') e,
            @OptionalBooleanArrayArgument([true, false]) f, @OptionalIntegerArrayArgument([1, 2, 3]) g,
            @OptionalFloatArrayArgument([2.2f, 3.3f]) h, @OptionalStringArrayArgument(['a', 'b']) i) {
            receivedA = a;
            receivedB = b;
            receivedC = c;
            receivedD = d;
            receivedE = e;
            receivedF = f;
            receivedG = g;
            receivedH = h;
            receivedI = i;
        }
    }
}
