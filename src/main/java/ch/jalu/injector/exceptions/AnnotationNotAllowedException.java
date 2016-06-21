package ch.jalu.injector.exceptions;

/**
 * Exception when an annotation class is provided to a method that does not allow
 * operations on instances via annotation classes.
 */
public class AnnotationNotAllowedException extends InjectorException {

    public AnnotationNotAllowedException(String message, Class<?> clazz) {
        super(message, clazz);
    }

}
