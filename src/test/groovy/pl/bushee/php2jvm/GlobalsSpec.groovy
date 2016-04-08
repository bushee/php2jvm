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
        def definedFunctions = globals.getDefinedFunctions()
        definedFunctions['internal'].containsValue('some_function1')
    }

    def "registered user function should be returned in defined functions list"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new UserFunctionHolder())

        then:
        def definedFunctions = globals.getDefinedFunctions()
        definedFunctions['user'].containsValue('some_function2')
    }

    def "many registered functions should not interfere with each other"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new UserFunctionHolder())
        globals.registerFunctions(new InternalFunctionHolder())

        then:
        def definedFunctions = globals.getDefinedFunctions()
        definedFunctions['internal'].containsValue('some_function1')
        definedFunctions['user'].containsValue('some_function2')
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

    def "calling function should delegate to actual annotated method"() {
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

    def "calling function with parameters should delegate to actual annotated method"() {
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

    def "calling function with missing parameters should result in using default values for them"() {
        given:
        def globals = new Globals()
        def functionHolder = new DefaultValuesFunctionHolder()
        globals.registerFunctions(functionHolder)

        when:
        globals.callFunction('default_values_function')

        then:
        functionHolder.receivedA == null
        functionHolder.receivedB == true
        functionHolder.receivedC == 3
        functionHolder.receivedD == 1.2f
        functionHolder.receivedE == 'abc'
        functionHolder.receivedF == [true, false]
        functionHolder.receivedG == [1, 2, 3]
        functionHolder.receivedH == [2.2f, 3.3f]
        functionHolder.receivedI == ['a', 'b']
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
