package ch.jalu.injector.handlers.postconstruct;

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
     * @param <T> the object's type
     * @return the new object to replace the instance with, null to keep the object the same
     * @throws Exception for validation errors or similar
     */
    @Nullable
    <T> T process(T object) throws Exception;

}
