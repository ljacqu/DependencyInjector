package ch.jalu.injector.exceptions;

/**
 * Parent of all injector exceptions.
 */
public class InjectorException extends RuntimeException {

    private final Class<?> clazz;

    public InjectorException(String message, Class<?> clazz) {
        super(message);
        this.clazz = clazz;
    }

    public InjectorException(String message, Throwable cause, Class<?> clazz) {
        super(message, cause);
        this.clazz = clazz;
    }

    /**
     * Returns the class an operation was being run for that caused the exception.
     *
     * @return the class
     */
    public Class<?> getClazz() {
        return clazz;
    }
}
