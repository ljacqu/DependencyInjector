package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.handlers.Handler;

import javax.annotation.Nullable;

/**
 * Handler fired before any object is constructed. Can be used to validate the
 * request class or to map it to a specific implementation.
 */
public interface PreConstructHandler extends Handler {

    /**
     * Processes an incoming request for instantiation for validation or custom mapping.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return the new class to use (e.g. concrete implementation type),
     *         or {@code null} to not modify the class
     * @throws Exception for failed validation or preconditions
     */
    @Nullable
    <T> Class<? extends T> accept(Class<T> clazz) throws Exception;

}
