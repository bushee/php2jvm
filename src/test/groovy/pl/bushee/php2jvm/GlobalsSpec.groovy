package pl.bushee.php2jvm

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

    def "should properly register overloaded function"() {
        given:
        def globals = new Globals()

        when:
        globals.registerFunctions(new OverloadedFunctionHolder())

        then:
        noExceptionThrown()
        def definedFunctions = globals.getDefinedFunctions()
        definedFunctions['user'].containsValue('some_function')
    }

    @PhpInternal
    private static class InternalFunctionHolder {
        @PhpFunction('some_function1')
        def someFunction1() {}
    }

    private static class UserFunctionHolder {
        @PhpFunction('some_function2')
        def someFunction2() {}
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

    private static class OverloadedFunctionHolder {
        @PhpFunction(value = 'some_function', overloaded = true)
        def someFunctionOverload1() {}

        @PhpFunction(value = 'some_function', overloaded = true)
        def someFunctionOverload2() {}
    }
}
