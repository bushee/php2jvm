package pl.bushee.php2jvm.basic.tokenizer;

import pl.bushee.php2jvm.*;

@PhpInternal
public class Tokenizer {

    @PhpFunction("token_name")
    public String tokenName(int token) {
        return "";
    }

    @PhpFunction(value = "token_get_all", overloaded = true)
    public Object[] tokenGetAll(String source) {
        return tokenGetAll(source, 0);
    }

    @PhpFunction(value = "token_get_all", overloaded = true)
    public Object[] tokenGetAll(String source, int flags) {
        return new Object[]{};
    }
}
