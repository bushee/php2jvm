package pl.bushee.php2jvm;

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

    public static Number toNumber(String string) {
        return null;
    }
}