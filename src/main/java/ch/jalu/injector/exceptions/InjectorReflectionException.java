package ch.jalu.injector.exceptions;

import java.lang.reflect.Member;

/**
 * Exception while trying to perform a reflection operation.
 */
public class InjectorReflectionException extends InjectorException {

    public InjectorReflectionException(String message, ReflectiveOperationException cause, Class<?> clazz) {
        super(message, cause, clazz);
    }

    public InjectorReflectionException(String message, ReflectiveOperationException cause, Member member) {
        super(message, cause, member.getDeclaringClass());
    }
}
