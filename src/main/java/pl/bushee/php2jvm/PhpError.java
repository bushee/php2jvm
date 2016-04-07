package pl.bushee.php2jvm;

public class PhpError extends RuntimeException {
    public PhpError(String message) {
        super(message);
    }
}
