package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectIdentifier;

import java.util.Collections;
import java.util.List;

/**
 * Simple object resolution: wraps an already existing object that should be used as an {@link Resolution}.
 */
public class SimpleResolution<T> implements Resolution<T> {

    private final T object;

    public SimpleResolution(T object) {
        this.object = object;
    }

    @Override
    public List<ObjectIdentifier> getDependencies() {
        return Collections.emptyList();
    }

    @Override
    public T instantiateWith(Object... values) {
        return object;
    }
}
