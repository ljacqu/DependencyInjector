package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.context.UnresolvedInstantiationContext;
import ch.jalu.injector.handlers.Handler;

/**
 * Handler fired before any object is constructed. Can be used to validate the
 * request class or to map it to a specific implementation.
 */
public interface PreConstructHandler extends Handler {

    /**
     * Processes an incoming request for instantiation for validation or custom mapping.
     *
     * @param context the instantiation context
     * @param <T> the class' type
     * @throws Exception for failed validation or preconditions
     */
    <T> void accept(UnresolvedInstantiationContext<T> context) throws Exception;

}
