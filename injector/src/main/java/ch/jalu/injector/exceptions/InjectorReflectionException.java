package ch.jalu.injector.exceptions;

/**
 * Exception while trying to perform a reflection operation.
 */
public class InjectorReflectionException extends InjectorException {

    public InjectorReflectionException(String message, Exception cause) {
        super(message, cause);
    }
}
