package pl.bushee.php2jvm

import spock.lang.Specification

class NumbersSpec extends Specification {

    def "isNumeric() should return true for any Number instance"() {
        expect:
        Numbers.isNumeric(number)

        where:
        number << [Stub(Number), Double.NaN, Float.NaN, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY]
    }

    def "isNumeric() should return true for properly formatted string"() {
        expect:
        // cast to Object to ensure that the more verbose version is used
        Numbers.isNumeric((Object) numericString)

        where:
        numericString << ['1', '2.', '.3', '4.5', '6e7', '.8e9', '10.11e12',
                          '+13', '+14.', '+.15', '+16.17', '+18e19', '+.20e21', '+22.23e24',
                          '-25', '-26.', '-.27', '-28.29', '-30e31', '-.32e33', '-34.35e36']
    }

    def "isNumeric() should return false for improperly formatted string"() {
        expect:
        // cast to Object to ensure that the more verbose version is used
        !Numbers.isNumeric((Object) notNumericString)

        where:
        notNumericString << ['surely not a number', '1.1.1', '+', '-', '.', '+-3', 'e', 'e1', '1e', '0x01', '0b01']
    }

    def "isNumeric() should return false for nor Number neither String instance"() {
        expect:
        !Numbers.isNumeric(new Object())
    }

    def "isNaN() should return true for any NaN value"() {
        expect:
        Numbers.isNaN(nan)

        where:
        nan << [Float.NaN, Double.NaN]
    }

    def "isNaN() should return false for any proper numeric value"() {
        expect:
        !Numbers.isNaN(notNan)

        where:
        notNan << [1, Float.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY]
    }

    def "isInfinite() should return true for any infinite numeric value"() {
        expect:
        Numbers.isInfinite(infinite)

        where:
        infinite << [Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY]
    }

    def "isInfinite() should return false for any finite numeric value or any non-numeric value"() {
        expect:
        !Numbers.isInfinite(notInfinite)

        where:
        notInfinite << [1, 12.5, '40', Float.NaN, Double.NaN, 'surely not finite']
    }

    def "isFinite() should return true for any finite numeric value"() {
        expect:
        Numbers.isFinite(finite)

        where:
        finite << [1, 12.5, '40', '1.5e13']
    }

    def "isFinite() should return false for any infinite numeric value or any non-numeric value"() {
        expect:
        !Numbers.isFinite(notFinite)

        where:
        notFinite << [Float.NaN, Double.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY]
    }
}
