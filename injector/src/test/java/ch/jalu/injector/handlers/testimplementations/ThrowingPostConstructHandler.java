package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;

/**
 * Post construct handler that throws an exception if it encounters an object of a given list of classes.
 */
public class ThrowingPostConstructHandler extends AbstractCountingHandler implements PostConstructHandler {

    private final Class<?>[] throwForClasses;

    public ThrowingPostConstructHandler(Class<?>... throwForClasses) {
        this.throwForClasses = throwForClasses;
    }

    @Override
    public <T> T process(T object, ResolvedInstantiationContext<T> context) {
        increment();
        for (Class<?> clazz : throwForClasses) {
            if (clazz.isInstance(object)) {
                throw new IllegalStateException("Class not allowed!");
            }
        }
        return null;
    }
}
