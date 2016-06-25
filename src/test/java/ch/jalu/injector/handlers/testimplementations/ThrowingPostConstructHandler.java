package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.exceptions.InjectorException;
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
    public void process(Object object) throws Exception {
        increment();
        for (Class<?> clazz : throwForClasses) {
            if (clazz.isInstance(object)) {
                throw new IllegalStateException("Class not allowed!");
            }
        }
    }
}
