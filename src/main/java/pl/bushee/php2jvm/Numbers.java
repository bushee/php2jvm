package pl.bushee.php2jvm;

import pl.bushee.php2jvm.function.PhpFunction;

import java.util.regex.Pattern;

@PhpInternal
public class Numbers {
    private static final Pattern IS_NUMERIC_MATCHER = Pattern.compile("^\\s*[+-]?[0-9]*(([0-9]\\.?)|(\\.[0-9]+))(e[0-9]+)?$");

    @PhpFunction("is_numeric")
    public static boolean isNumeric(Object object) {
        return object instanceof Number
            || (object instanceof String && isNumeric((String) object));
    }

    public static boolean isNumeric(String string) {
        return IS_NUMERIC_MATCHER.matcher(string).matches();
    }

    @PhpFunction("is_nan")
    public static boolean isNaN(Object number) {
        return (number instanceof Double && Double.isNaN((Double) number))
            || (number instanceof Float && Float.isNaN((Float) number));
    }

    @PhpFunction("is_infinite")
    public static boolean isInfinite(Object number) {
        return (number instanceof Double && Double.isInfinite((Double) number))
            || (number instanceof Float && Float.isInfinite((Float) number));
    }

    @PhpFunction("is_finite")
    public static boolean isFinite(Object number) {
        if (isNaN(number)) {
            return false;
        }
        if (number instanceof Double) {
            return Double.isFinite((Double) number);
        }
        if (number instanceof Float) {
            return Float.isFinite((Float) number);
        }
        return isNumeric(number);
    }

    public static Number toNumber(String string) {
        return null;
    }
}