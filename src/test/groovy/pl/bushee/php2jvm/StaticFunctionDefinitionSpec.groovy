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

class StaticFunctionDefinitionSpec extends Specification {
    def staticMethod = StaticFunctionDefinitionSpec.getMethods().find { it.name == 'staticMethod' }
    def methodWithDefaults = StaticFunctionDefinitionSpec.getMethods().find { it.name == 'methodWithDefaults' }
    def instanceMethod = StaticFunctionDefinitionSpec.getMethods().find { it.name == 'instanceMethod' }

    def "should throw exception if trying to instantiate StaticFunctionDefinition with an instance method"() {
        when:
        new StaticFunctionDefinition(instanceMethod)

        then:
        thrown IllegalArgumentException
    }

    def "calling function with parameters should delegate to actual passed method"() {
        given:
        def functionDefinition = new StaticFunctionDefinition(staticMethod)

        when:
        def result = functionDefinition.call('a', 'b', 'c')

        then:
        result == 'concatenation result = "abc"'
    }

    def "calling function with too little parameters should result in passing nulls for missing ones without default values"() {
        given:
        def functionDefinition = new StaticFunctionDefinition(staticMethod)

        when:
        def result = functionDefinition.call('a')

        then:
        result == 'concatenation result = "anullnull"'
    }

    def "calling function with too many parameters should result in ignoring the extra ones"() {
        given:
        def functionDefinition = new StaticFunctionDefinition(staticMethod)

        when:
        def result = functionDefinition.call('a', 'b', 'c', 'd', 'e')

        then:
        result == 'concatenation result = "abc"'
    }

    @Unroll
    def "calling function with missing parameters should result in using default values for them (passing #argsCount out of 9 arguments)"() {
        given:
        def functionDefinition = new StaticFunctionDefinition(methodWithDefaults)

        when:
        functionDefinition.call(arguments.toArray())

        then:
        receivedA == expectedA
        receivedB == expectedB
        receivedC == expectedC
        receivedD == expectedD
        receivedE == expectedE
        receivedF == expectedF
        receivedG == expectedG
        receivedH == expectedH
        receivedI == expectedI

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

    @PhpFunction('')
    def static staticMethod(a, b, c) {
        return "concatenation result = \"$a$b$c\""
    }

    static receivedA, receivedB, receivedC, receivedD, receivedE, receivedF, receivedG, receivedH, receivedI

    @PhpFunction('')
    def static methodWithDefaults(
        @OptionalNullArgument a,
        @OptionalBooleanArgument(true) b, @OptionalIntegerArgument(3) c,
        @OptionalFloatArgument(1.2f) d, @OptionalStringArgument('abc') e,
        @OptionalBooleanArrayArgument([true, false]) f, @OptionalIntegerArrayArgument([1, 2, 3]) g,
        @OptionalFloatArrayArgument([2.2f, 3.3f]) h, @OptionalStringArrayArgument(['a', 'b']) i
    ) {
        receivedA = a
        receivedB = b
        receivedC = c
        receivedD = d
        receivedE = e
        receivedF = f
        receivedG = g
        receivedH = h
        receivedI = i
    }

    @PhpFunction('')
    def instanceMethod() {}
}
