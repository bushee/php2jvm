package pl.bushee.php2jvm

import pl.bushee.php2jvm.function.PhpFunction
import spock.lang.Specification

import java.lang.reflect.Method

class FunctionDefinitionSpec extends Specification {

    def "isInternal() should return true for internal function"() {
        given:
        def method = InternalFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method, 'some_name')

        then:
        function.isInternal()
    }

    def "isInternal() should return false for user function"() {
        given:
        def method = UserFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method, 'some_name')

        then:
        !function.isInternal()
    }

    def "getType() should return INTERNAL for internal function"() {
        given:
        def method = InternalFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method, 'some_name')

        then:
        function.getType() == FunctionDefinition.FunctionType.INTERNAL
    }

    def "getType() should return USER for user function"() {
        given:
        def method = UserFunctionHolder.getMethod('someFunction')

        when:
        def function = new FunctionDefinitionImpl(method, 'some_name')

        then:
        function.getType() == FunctionDefinition.FunctionType.USER
    }

    private static class FunctionDefinitionImpl extends FunctionDefinition {
        private FunctionDefinitionImpl(Method method, String name) {
            super(method, name)
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
}
