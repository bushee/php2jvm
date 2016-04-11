package pl.bushee.php2jvm.basic.tokenizer;

import pl.bushee.php2jvm.PhpInternal;
import pl.bushee.php2jvm.function.OptionalIntegerArgument;
import pl.bushee.php2jvm.function.PhpFunction;

@PhpInternal
public class Tokenizer {

    @PhpFunction("token_name")
    public static String tokenName(int token) {
        return "";
    }

    @PhpFunction(value = "token_get_all")
    public static Object[] tokenGetAll(String source, @OptionalIntegerArgument(0) int flags) {
        return new Object[]{};
    }
}
