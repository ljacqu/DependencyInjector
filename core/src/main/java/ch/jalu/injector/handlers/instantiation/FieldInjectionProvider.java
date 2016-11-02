package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider for {@link FieldInjection}.
 */
public class FieldInjectionProvider extends DirectInstantiationProvider {

    @Override
    protected <T> FieldInjection<T> safeGet(Class<T> clazz) {
        Constructor<T> constructor = getNoArgsConstructor(clazz);
        if (constructor == null) {
            return null;
        }
        List<Field> fields = getInjectionFields(clazz);
        if (fields.isEmpty()) {
            return null;
        }
        validateHasNoOtherInjectAnnotations(clazz);
        return new FieldInjection<>(constructor, fields);
    }

    private static List<Field> getInjectionFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : ReflectionUtils.safeGetDeclaredFields(clazz)) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new InjectorException(String.format("Field '%s' in class '%s' is static but "
                        + "annotated with @Inject", field.getName(), clazz.getSimpleName()));
                }
                fields.add(field);
            }
        }
        return fields;
    }

    private static void validateHasNoOtherInjectAnnotations(Class<?> clazz) {
        if (InjectorUtils.isInjectAnnotationPresent(clazz.getDeclaredConstructors())
            || InjectorUtils.isInjectAnnotationPresent(ReflectionUtils.safeGetDeclaredMethods(clazz))) {
            throw new InjectorException("Class '" + clazz + "' may not have @Inject on fields AND on other members. "
                    + "Remove other @Inject uses or remove it from the fields");
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getNoArgsConstructor(Class<T> clazz) {
        try {
            Constructor<?> noArgsConstructor = clazz.getDeclaredConstructor();
            noArgsConstructor.setAccessible(true);
            return (Constructor<T>) noArgsConstructor;
        } catch (NoSuchMethodException ignore) {
            // no-arg constructor not available
        }
        return null;
    }
}
