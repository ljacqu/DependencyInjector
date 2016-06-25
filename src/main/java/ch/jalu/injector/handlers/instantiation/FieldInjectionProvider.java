package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Provider for {@link FieldInjection}.
 */
public class FieldInjectionProvider implements InstantiationProvider {

    @Override
    public <T> FieldInjection<T> get(Class<T> clazz) {
        Constructor<T> constructor = getNoArgsConstructor(clazz);
        if (constructor == null) {
            return null;
        }
        List<Field> fields = getInjectionFields(clazz);
        return fields.isEmpty() ? null : new FieldInjection<>(constructor, fields);
    }

    private static List<Field> getInjectionFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Inject.class)) {
                if (Modifier.isStatic(field.getModifiers())) {
                    throw new InjectorException(String.format("Field '%s' in class '%s' is static but "
                            + "annotated with @Inject", field.getName(), clazz.getSimpleName()), clazz);
                }
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getNoArgsConstructor(Class<T> clazz) {
        try {
            Constructor<?> noArgsConstructor = clazz.getDeclaredConstructor();
            noArgsConstructor.setAccessible(true);
            return (Constructor<T>) noArgsConstructor;
        } catch (NoSuchMethodException ignore) {
            // no no-arg constructor available
        }
        return null;
    }
}
