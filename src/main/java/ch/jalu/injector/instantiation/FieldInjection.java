package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Functionality for field injection.
 */
public class FieldInjection<T> implements Instantiation<T> {

    private final Field[] fields;
    private final Constructor<T> defaultConstructor;

    private FieldInjection(Constructor<T> defaultConstructor, Collection<Field> fields) {
        this.fields = fields.toArray(new Field[fields.size()]);
        this.defaultConstructor = defaultConstructor;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        List<DependencyDescription> dependencies = new ArrayList<>(fields.length);
        for (Field field : fields) {
            dependencies.add(new DependencyDescription(
                field.getType(), field.getGenericType(), field.getAnnotations()));
        }
        return dependencies;
    }

    @Override
    public T instantiateWith(Object... values) {
        InjectorUtils.checkArgument(values.length == fields.length,
            "The number of values must be equal to the number of fields", defaultConstructor.getDeclaringClass());

        T instance = ReflectionUtils.newInstance(defaultConstructor);
        for (int i = 0; i < fields.length; ++i) {
            InjectorUtils.checkNotNull(values[i], defaultConstructor.getDeclaringClass());
            ReflectionUtils.setField(fields[i], instance, values[i]);
        }
        return instance;
    }

    /**
     * Returns a provider for a {@code FieldInjection<T>} instance, i.e. a provides an object
     * with which field injection can be performed on the given class if applicable. The provided
     * value is {@code null} if field injection cannot be applied to the class.
     *
     * @param clazz the class to provide field injection for
     * @param <T> the class' type
     * @return field injection provider for the given class, or null if not applicable
     */
    public static <T> Provider<FieldInjection<T>> provide(final Class<T> clazz) {
        return new Provider<FieldInjection<T>>() {
            @Override
            public FieldInjection<T> get() {
                Constructor<T> constructor = getNoArgsConstructor(clazz);
                if (constructor == null) {
                    return null;
                }
                List<Field> fields = getInjectionFields(clazz);
                return fields.isEmpty() ? null : new FieldInjection<>(constructor, fields);
            }
        };
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
