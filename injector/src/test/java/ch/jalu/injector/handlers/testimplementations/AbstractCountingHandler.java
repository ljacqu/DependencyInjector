package ch.jalu.injector.handlers.testimplementations;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract handler that counts its invocations.
 */
public abstract class AbstractCountingHandler {

    private final List<Object> entries = new ArrayList<>();
    private int counter = 0;

    protected final void increment(Object entry) {
        ++counter;
        entries.add(entry);
    }

    public final int getCounter() {
        return counter;
    }
}
