package ch.jalu.injector.handlers.instantiation;

import javax.inject.Inject;
import java.lang.reflect.Constructor;

/**
 * Provider for {@link ConstructorInjection}.
 */
public class ConstructorInjectionProvider implements InstantiationProvider {

    @Override
    public <T> ConstructorInjection<T> get(Class<T> clazz) {
        Constructor<T> constructor = getInjectionConstructor(clazz);
        return constructor == null ? null : new ConstructorInjection<>(constructor);
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
                return (Constructor<T>) constructor;
            }
        }
        return null;
    }
}
