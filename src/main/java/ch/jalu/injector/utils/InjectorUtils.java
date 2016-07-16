package ch.jalu.injector.utils;

import ch.jalu.injector.exceptions.InjectorException;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
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
    @SafeVarargs
    public static <T> T firstNotNull(T... objects) {
        for (T o : objects) {
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    public static boolean canInstantiate(Class<?> clazz) {
        return !clazz.isEnum() && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }

    public static String getDeclarer(@Nullable Member member) {
        return member == null ? "null" : member.getDeclaringClass().getName();
    }

    public static <A extends AccessibleObject> boolean isInjectAnnotationPresent(A[] accessibles) {
        for (A accessible : accessibles) {
            if (accessible.isAnnotationPresent(Inject.class)) {
                return true;
            }
        }
        return false;
    }
}
