package ch.jalu.injector.utils;

import ch.jalu.injector.exceptions.InjectorReflectionException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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
                "Could not get value of field '" + field.getName() + "' for " + instance, e, field);
        }
    }

    /**
     * Sets the given field on the given instance.
     *
     * @param field the field to set
     * @param instance the instance to set the field in  ({@code null} for static fields)
     * @param value the value to set
     */
    public static void setField(Field field, Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
        } catch (IllegalAccessException e) {
            throw new InjectorReflectionException(
                "Could not set field '" + field.getName() + "' for " + instance, e, field);
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
                "Could not invoke method '" + method.getName() + "' for " + instance, e, method);
        }
    }

    /**
     * Returns all fields of the given class annotated with the given annotation. Does not consider parent classes.
     *
     * @param clazz the class to process
     * @param annotation the annotation to search fields for
     * @return fields in clazz having the given annotation
     */
    public static List<Field> getFieldsWithAnnotation(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation)) {
                fields.add(field);
            }
        }
        return fields;
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
        try {
            return constructor.newInstance(parameters);
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new InjectorReflectionException("Could not invoke constructor of class '"
                    + constructor.getDeclaringClass() + "'", e, constructor);
        }
    }
}
