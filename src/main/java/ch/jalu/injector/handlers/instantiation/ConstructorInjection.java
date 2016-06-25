package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Functionality for constructor injection.
 */
public class ConstructorInjection<T> implements Instantiation<T> {

    private final Constructor<T> constructor;

    protected ConstructorInjection(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        Class<?>[] parameters = constructor.getParameterTypes();
        Type[] genericTypes = constructor.getGenericParameterTypes();
        Annotation[][] annotations = constructor.getParameterAnnotations();

        List<DependencyDescription> dependencies = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; ++i) {
            dependencies.add(new DependencyDescription(parameters[i], genericTypes[i], annotations[i]));
        }
        return dependencies;
    }

    @Override
    public T instantiateWith(Object... values) {
        InjectorUtils.checkNotNull(values, constructor.getDeclaringClass());
        return ReflectionUtils.newInstance(constructor, values);
    }
}
