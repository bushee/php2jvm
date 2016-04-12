package pl.bushee.php2jvm

import pl.bushee.php2jvm.function.PhpFunction
import spock.lang.Specification

import java.lang.reflect.Method

class FunctionDefinitionSpec extends Specification {

    def "isInternal() should return true for internal function"() {
        given:
        def method = InternalFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method)

        then:
        function.isInternal()
    }

    def "isInternal() should return false for user function"() {
        given:
        def method = UserFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method)

        then:
        !function.isInternal()
    }

    def "getType() should return INTERNAL for internal function"() {
        given:
        def method = InternalFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method)

        then:
        function.getType() == FunctionDefinition.FunctionType.INTERNAL
    }

    def "getType() should return USER for user function"() {
        given:
        def method = UserFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method)

        then:
        function.getType() == FunctionDefinition.FunctionType.USER
    }

    def "function definition initialization should fail if passed method was not annotated properly"() {
        given:
        def method = NotAFunctionHolder.getMethod('someMethodButNotAFunction')

        when:
        new FunctionDefinitionImpl(method)

        then:
        thrown IllegalArgumentException
    }

    private static class FunctionDefinitionImpl extends FunctionDefinition {
        private FunctionDefinitionImpl(Method method) {
            super(method)
        }

        @Override
        Object call(Object... arguments) throws IllegalAccessException {
            return null
        }
    }

    @PhpInternal
    private static class InternalFunctionHolder {
        @PhpFunction('some_name')
        def someFunction() {}
    }

    private static class UserFunctionHolder {
        @PhpFunction('some_name')
        def someFunction() {}
    }

    private static class NotAFunctionHolder {
        def someMethodButNotAFunction() {}
    }
}
