package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.instantiation.ConstructorInjection;
import ch.jalu.injector.instantiation.FieldInjection;
import ch.jalu.injector.instantiation.Instantiation;
import ch.jalu.injector.instantiation.InstantiationFallback;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import javax.inject.Provider;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Helper class for functions relating to injecting.
 */
public class InjectionHelper {

    private InjectionHelper() {
    }

    /**
     * Returns the {@link Instantiation} for the given class, or null if none applicable.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return injection of the class or null if none detected
     */
    @Nullable
    public static <T> Instantiation<T> getInjection(Class<T> clazz) {
        return firstNotNull(
            ConstructorInjection.provide(clazz),
            FieldInjection.provide(clazz),
            InstantiationFallback.provide(clazz));
    }

    /**
     * Validates and locates the given class' post construct method. Returns {@code null} if none present.
     *
     * @param clazz the class to search
     * @return post construct method, or null
     */
    @Nullable
    public static Method getAndValidatePostConstructMethod(Class<?> clazz) {
        Method postConstructMethod = null;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (postConstructMethod != null) {
                    throw new InjectorException("Multiple methods with @PostConstruct on " + clazz);
                } else if (method.getParameterTypes().length > 0 || Modifier.isStatic(method.getModifiers())) {
                    throw new InjectorException("@PostConstruct method may not be static or have any parameters. "
                        + "Invalid method in " + clazz);
                } else if (method.getReturnType() != void.class) {
                    throw new InjectorException("@PostConstruct method must have return type void. "
                        + "Offending class: " + clazz);
                } else {
                    postConstructMethod = method;
                }
            }
        }
        return postConstructMethod;
    }

    @SafeVarargs
    private static <T> Instantiation<T> firstNotNull(Provider<? extends Instantiation<T>>... providers) {
        for (Provider<? extends Instantiation<T>> provider : providers) {
            Instantiation<T> object = provider.get();
            if (object != null) {
                return object;
            }
        }
        return null;
    }
}
