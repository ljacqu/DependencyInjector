package ch.jalu.injector.utils;

import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.annotations.NoMethodScan;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.exceptions.InjectorReflectionException;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Reflection methods.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {
    }

    /**
     * Gets the value of the given field in the given instance.
     *
     * @param field the field to retrieve
     * @param instance the instance to retrieve it from ({@code null} for static fields)
     * @return the field's value
     */
    public static Object getFieldValue(Field field, Object instance) {
        field.setAccessible(true);
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new InjectorReflectionException(
                "Could not get value of field '" + field.getName() + "' for " + instance, e);
        }
    }

    /**
     * Sets the given field on the given instance.
     *
     * @param field the field to set
     * @param instance the instance to set the field in ({@code null} for static fields)
     * @param value the value to set
     */
    public static void setField(Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new InjectorReflectionException(
                "Could not set field '" + field.getName() + "' for " + instance, e);
        }
    }

    /**
     * Invokes the given method on the given instance with the provided parameters.
     *
     * @param method the method to invoke
     * @param instance the instance to invoke it from ({@code null} for static methods)
     * @param parameters the parameters to use
     * @return the returned value from the method invocation
     */
    public static Object invokeMethod(Method method, Object instance, Object... parameters) {
        method.setAccessible(true);
        try {
            return method.invoke(instance, parameters);
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new InjectorReflectionException(
                "Could not invoke method '" + method.getName() + "' for " + instance, e);
        }
    }

    /**
     * Invokes the given constructor with the provided parameters.
     *
     * @param constructor the constructor
     * @param parameters the parameters to invoke the constructor with
     * @param <T> the type the constructor belongs to
     * @return the instantiated object
     */
    public static <T> T newInstance(Constructor<T> constructor, Object... parameters) {
        constructor.setAccessible(true);
        try {
            return constructor.newInstance(parameters);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new InjectorReflectionException("Could not invoke constructor of class '"
                    + constructor.getDeclaringClass() + "'", e);
        }
    }

    @Nullable
    public static Class<?> getGenericClass(Class<?> mainType, @Nullable Type genericType) {
        if (mainType.isArray()) {
            return mainType.getComponentType();
        } else if (genericType instanceof ParameterizedType && Iterable.class.isAssignableFrom(mainType)) {
            return (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
        }
        return null;
    }

    /**
     * Attempts to convert the given {@code result} Set to the given collection type. If the given class
     * to match is an array, an array will be returned without any type checks. Otherwise, tries to satisfy
     * the field by returning a Set or a List instance. Throws an exception if no type could be used.
     *
     * @param rawType the collection type to match
     * @param result the result Set to potentially convert
     * @param <T> the set's type
     * @return result set as suitable collection type
     */
    public static <T> Object toSuitableCollectionType(Class<?> rawType, Set<T> result) {
        if (rawType.isArray()) {
            @SuppressWarnings("unchecked")
            Class<T[]> arrayClass = (Class<T[]>) rawType;
            return Arrays.copyOf(result.toArray(), result.size(), arrayClass);
        } else if (rawType.isAssignableFrom(Set.class)) {
            return result;
        } else if (rawType.isAssignableFrom(List.class)) {
            return new ArrayList<>(result);
        }
        throw new InjectorException("Cannot convert @AllTypes result to '" + rawType + "'. "
            + "Supported: Set, List, or any subtype thereof, and array");
    }

    /**
     * Returns all methods of a class if not annotated with {@link NoMethodScan}. Otherwise returns an empty array.
     *
     * @param clazz the class to process
     * @return the class' methods or empty array if methods should not be scanned
     */
    public static Method[] safeGetDeclaredMethods(Class<?> clazz) {
        return clazz.isAnnotationPresent(NoMethodScan.class)
            ? new Method[0]
            : clazz.getDeclaredMethods();
    }

    /**
     * Returns all fields of a class if not annotated with {@link NoFieldScan}. Otherwise returns an empty array.
     *
     * @param clazz the class to process
     * @return the class' fields or empty array if fields should not be scanned
     */
    public static Field[] safeGetDeclaredFields(Class<?> clazz) {
        return clazz.isAnnotationPresent(NoFieldScan.class)
            ? new Field[0]
            : clazz.getDeclaredFields();
    }
}
