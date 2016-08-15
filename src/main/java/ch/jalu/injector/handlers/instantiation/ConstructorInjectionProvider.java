package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Constructor;

/**
 * Provider for {@link ConstructorInjection}.
 */
public class ConstructorInjectionProvider extends DirectInstantiationProvider {

    @Override
    protected <T> ConstructorInjection<T> safeGet(Class<T> clazz) {
        Constructor<T> constructor = getInjectionConstructor(clazz);
        if (constructor == null) {
            return null;
        }
        validateHasNoOtherInjectAnnotations(clazz);
        return new ConstructorInjection<>(constructor);
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

    private static void validateHasNoOtherInjectAnnotations(Class<?> clazz) {
        if (InjectorUtils.isInjectAnnotationPresent(ReflectionUtils.safeGetDeclaredFields(clazz))
                || InjectorUtils.isInjectAnnotationPresent(ReflectionUtils.safeGetDeclaredMethods(clazz))) {
            throw new InjectorException("Class '" + clazz + "' may not have @Inject on a constructor AND "
                + "on other members. Remove other @Inject uses or remove it from the constructor");
        }
    }
}
