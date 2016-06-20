package ch.jalu.injector.utils;

import ch.jalu.injector.exceptions.InjectorException;

/**
 * Class with simple utility methods.
 */
public class InjectorUtils {

    private InjectorUtils() {
    }

    public static void checkNotNull(Object o) {
        checkNotNull(o, "Object may not be null");
    }

    public static void checkNotNull(Object o, String errorMessage) {
        if (o == null) {
            throw new InjectorException(errorMessage);
        }
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new InjectorException(errorMessage);
        }
    }
}
