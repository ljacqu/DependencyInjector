package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Functionality for field injection.
 */
public class FieldInjection<T> implements Instantiation<T> {

    private final Field[] fields;
    private final Constructor<T> defaultConstructor;

    protected FieldInjection(Constructor<T> defaultConstructor, Collection<Field> fields) {
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
}
