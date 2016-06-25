package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.handlers.Handler;

/**
 * Handler for objects after their construction.
 */
public interface PostConstructHandler extends Handler {

    /**
     * Processes the newly created object.
     *
     * @param object the object that was instantiated
     * @throws Exception for validation errors or similar
     */
    void process(Object object) throws Exception;

}
