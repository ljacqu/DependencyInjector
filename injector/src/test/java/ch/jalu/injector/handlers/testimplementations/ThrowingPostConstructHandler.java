package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.handlers.Handler;

/**
 * Post construct handler that throws an exception if it encounters an object of a given list of classes.
 */
public class ThrowingPostConstructHandler extends AbstractCountingHandler implements Handler {

    private final Class<?>[] throwForClasses;

    public ThrowingPostConstructHandler(Class<?>... throwForClasses) {
        this.throwForClasses = throwForClasses;
    }

    @Override
    public <T> T postProcess(T object, ResolvedContext context) {
        increment();
        for (Class<?> clazz : throwForClasses) {
            if (clazz.isInstance(object)) {
                throw new IllegalStateException("Class not allowed!");
            }
        }
        return null;
    }
}
