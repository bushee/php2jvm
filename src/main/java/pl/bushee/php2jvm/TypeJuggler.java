package pl.bushee.php2jvm;

public class TypeJuggler {

    public static ComparisonResult compare(Object a, Object b) {
        if (a == b) {
            return ComparisonResult.EQUALS;
        }
        if (a instanceof String) {
            if (b instanceof String) {
                return compare((String) a, (String) b);
            }
            if (b == null) {
                return compare((String) a, "");
            }
        }
        if (a == null && b instanceof String) {
            return compare("", (String) b);
        }
        return ComparisonResult.EQUALS;
    }

    public static ComparisonResult compare(String a, String b) {
        if (Numbers.isNumeric(a) && Numbers.isNumeric(b)) {
            return compare(Numbers.toNumber(a), Numbers.toNumber(b));
        }
        return ComparisonResult.fromInt(a.compareTo(b));
    }

    public static ComparisonResult compare(Number a, Number b) {
        return ComparisonResult.EQUALS;
    }

    public enum ComparisonResult {
        LESS_THAN(-1, false), EQUALS(0, true), GREATER_THAN(1, false);

        private final int intValue;
        private final boolean isEqual;

        ComparisonResult(int intValue, boolean isEqual) {
            this.intValue = intValue;
            this.isEqual = isEqual;
        }

        public int getIntValue() {
            return intValue;
        }

        public boolean isEqual() {
            return isEqual;
        }

        public static ComparisonResult fromInt(int intValue) {
            if (intValue < 0) {
                return LESS_THAN;
            }
            if (intValue > 0) {
                return GREATER_THAN;
            }
            return EQUALS;
        }
    }
}
