package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.handlers.Handler;

import javax.annotation.Nullable;

/**
 * Handler for objects after their construction.
 */
public interface PostConstructHandler extends Handler {

    /**
     * Processes the newly created object.
     *
     * @param object the object that was instantiated
     * @param context the instantiation context
     * @param <T> the object's type
     * @return the new object to replace the instance with, null to keep the object the same
     * @throws Exception for validation errors or similar
     */
    @Nullable
    <T> T process(T object, ResolvedInstantiationContext<T> context) throws Exception;

}
