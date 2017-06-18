package ch.jalu.injector.utils;

import ch.jalu.injector.exceptions.InjectorException;

import javax.annotation.Nullable;
import java.lang.reflect.Modifier;

/**
 * Class with simple utility methods.
 */
public final class InjectorUtils {

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

    @SafeVarargs
    public static <T> void checkNoNullValues(T... arr) {
        checkNotNull(arr);
        for (Object o : arr) {
            checkNotNull(o);
        }
    }

    /**
     * Returns whether the given array contains a null value or not.
     *
     * @param arr the array to process
     * @return true if at least one entry is null, false otherwise
     */
    public static boolean containsNullValue(Object... arr) {
        for (Object o : arr) {
            if (o == null) {
                return true;
            }
        }
        return false;
    }

    public static void checkArgument(boolean expression, String errorMessage) {
        if (!expression) {
            throw new InjectorException(errorMessage);
        }
    }

    public static void rethrowException(Exception e) throws InjectorException {
        throw (e instanceof InjectorException)
                ? (InjectorException) e
                : new InjectorException("An error occurred (see cause)", e);
    }

    @Nullable
    public static <T> T firstNotNull(T obj1, T obj2) {
        return obj1 == null ? obj2 : obj1;
    }

    public static boolean canInstantiate(Class<?> clazz) {
        return !clazz.isEnum() && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }
}
