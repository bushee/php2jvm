package pl.bushee.php2jvm;

interface FunctionDefinition {

    FunctionType getType();

    Object call(Object... arguments);

    enum FunctionType {
        INTERNAL, USER;
    }
}
