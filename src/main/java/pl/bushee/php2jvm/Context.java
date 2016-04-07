package pl.bushee.php2jvm;

public class Context {

    private final Globals globals;

    Context() {
        this.globals = (Globals) this;
    }

    public Context(Globals globals) {
        this.globals = globals;
    }

    public Globals getGlobals() {
        return globals;
    }
}
