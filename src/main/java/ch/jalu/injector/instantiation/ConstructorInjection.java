package ch.jalu.injector.instantiation;

import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Inject;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;

/**
 * Functionality for constructor injection.
 */
public class ConstructorInjection<T> implements Instantiation<T> {

    private final Constructor<T> constructor;

    private ConstructorInjection(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public Class<?>[] getDependencies() {
        return constructor.getParameterTypes();
    }

    @Override
    public Annotation[][] getDependencyAnnotations() {
        return constructor.getParameterAnnotations();
    }

    @Override
    public T instantiateWith(Object... values) {
        InjectorUtils.checkNotNull(values, constructor.getDeclaringClass());
        return ReflectionUtils.newInstance(constructor, values);
    }

    public static <T> Provider<ConstructorInjection<T>> provide(final Class<T> clazz) {
        return new Provider<ConstructorInjection<T>>() {
            @Override
            public ConstructorInjection<T> get() {
                Constructor<T> constructor = getInjectionConstructor(clazz);
                return constructor == null ? null : new ConstructorInjection<>(constructor);
            }
        };
    }


    /**
     * Gets the first found constructor annotated with {@link Inject} of the given class
     * and marks it as accessible.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return injection constructor for the class, null if not applicable
     */
    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> getInjectionConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                constructor.setAccessible(true);
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }

}
