package ch.jalu.injector.handlers.dependency.providers;

/**
 * Delta implementation.
 */
public class Delta1 implements Delta {

    private final String name;

    // Passing a primitive type also ensures that we're really not going to instantiate
    // this over another instantiation method, should we misconfigure this test in the future
    public Delta1(Bravo bravo, int i) {
        name = bravo.getServiceName() + i;
    }

    @Override
    public String getName() {
        return name;
    }
}
