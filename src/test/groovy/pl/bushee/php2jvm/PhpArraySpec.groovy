package pl.bushee.php2jvm

import spock.lang.Specification

class PhpArraySpec extends Specification {

    def '$a[] = 1'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)

        then:
        array == ['0': 1]
        array.get(0) == 1
        array.get('0') == 1
    }

    def '$a[] = 1; $a[] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)
        array.append(2)

        then:
        array == ['0': 1, '1': 2]
        array.get(0) == 1
        array.get('0') == 1
        array.get(1) == 2
        array.get('1') == 2
    }

    def '$a["a"] = 1'() {
        given:
        def array = new PhpArray()

        when:
        array.put('a', 1)

        then:
        array == ['a': 1]
        array.get('a') == 1
    }

    def '$a["a"] = 1; $a["b"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('a', 1)
        array.put('b', 2)

        then:
        array == ['a': 1, 'b': 2]
        array.get('a') == 1
        array.get('b') == 2
    }

    def '$a["a"] = 1; $a[] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('a', 1)
        array.append(2)

        then:
        array == ['a': 1, '0': 2]
        array.get('a') == 1
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a[] = 1; $a["a"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)
        array.put('a', 2)

        then:
        array == ['0': 1, 'a': 2]
        array.get(0) == 1
        array.get('0') == 1
        array.get('a') == 2
    }

    def '$a[4] = 1'() {
        given:
        def array = new PhpArray()

        when:
        array.put(4, 1)

        then:
        array == ['4': 1]
        array.get(4) == 1
        array.get('4') == 1
    }

    def '$a[4] = 1; $a[] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put(4, 1)
        array.append(2)

        then:
        array == ['4': 1, '5': 2]
        array.get(4) == 1
        array.get('4') == 1
        array.get(5) == 2
        array.get('5') == 2
    }

    def '$a["4"] = 1'() {
        given:
        def array = new PhpArray()

        when:
        array.put('4', 1)

        then:
        array == ['4': 1]
        array.get(4) == 1
        array.get('4') == 1
    }

    def '$a["4"] = 1; $a[] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('4', 1)
        array.append(2)

        then:
        array == ['4': 1, '5': 2]
        array.get(4) == 1
        array.get('4') == 1
        array.get(5) == 2
        array.get('5') == 2
    }

    def '$a[] = 1; $a[0] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)
        array.put(0, 2)

        then:
        array == ['0': 2]
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a[] = 1; $a["0"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)
        array.put('0', 2)

        then:
        array == ['0': 2]
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a[0] = 1; $a["0"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put(0, 1)
        array.put('0', 2)

        then:
        array == ['0': 2]
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a["0"] = 1; $a[0] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('0', 1)
        array.put(0, 2)

        then:
        array == ['0': 2]
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a["0"] = 1; $a["0"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('0', 1)
        array.put('0', 2)

        then:
        array == ['0': 2]
        array.get(0) == 2
        array.get('0') == 2
    }

    def '$a["a"] = 1; $a["a"] = 2'() {
        given:
        def array = new PhpArray()

        when:
        array.put('a', 1)
        array.put('a', 2)

        then:
        array == ['a': 2]
        array.get('a') == 2
    }

    def '$a[] = 1; $a[5] == null'() {
        given:
        def array = new PhpArray()

        when:
        array.append(1)

        then:
        array.get(5) == null
    }

    def '$a = array(1) + array(2)'() {
        given:
        def a = new PhpArray()
        a.append(1)
        def b = new PhpArray()
        b.append(2)

        when:
        def c = PhpArray.union(a, b)

        then:
        c == ['0': 1]
        c.get(0) == 1
        c.get('0') == 1
    }

    def '$a = array(1) + array(2 => 2)'() {
        given:
        def a = new PhpArray()
        a.append(1)
        def b = new PhpArray()
        b.put(2, 2)

        when:
        def c = PhpArray.union(a, b)

        then:
        c == ['0': 1, '2': 2]
        c.get(0) == 1
        c.get('0') == 1
        c.get(2) == 2
        c.get('2') == 2
    }

    def '$a = array(1); $b = array(1); $a == $b && $b == $a'() {
        when:
        def a = new PhpArray()
        a.append(1)
        def b = new PhpArray()
        b.append(1)

        then:
        a.equals(b)
        b.equals(a)
    }

    def '$a = array(1); $b = array(1); $a !== $b && $b !== $a'() {
        when:
        def a = new PhpArray()
        a.append(1)
        def b = new PhpArray()
        b.append(1)

        then:
        !a.identicalTo(b)
        !b.identicalTo(a)
    }
}
