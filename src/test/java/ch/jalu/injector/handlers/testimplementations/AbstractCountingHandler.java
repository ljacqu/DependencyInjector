package ch.jalu.injector.handlers.testimplementations;

/**
 * Abstract handler that counts its invocations.
 */
public abstract class AbstractCountingHandler {

    private int counter = 0;

    protected final void increment() {
        ++counter;
    }

    public final int getCounter() {
        return counter;
    }
}
