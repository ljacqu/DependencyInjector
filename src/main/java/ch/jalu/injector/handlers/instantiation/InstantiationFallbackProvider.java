package ch.jalu.injector.handlers.instantiation;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;

/**
 * Provider for {@link InstantiationFallback}.
 */
public class InstantiationFallbackProvider implements InstantiationProvider {

    @Override
    public <T> InstantiationFallback<T> get(Class<T> clazz) {
        Constructor<T> noArgsConstructor = getNoArgsConstructor(clazz);
        // Return fallback only if we have no args constructor and no @Inject annotation anywhere
        if (noArgsConstructor != null
                && !isInjectionAnnotationPresent(clazz.getDeclaredConstructors())
                && !isInjectionAnnotationPresent(clazz.getDeclaredFields())
                && !isInjectionAnnotationPresent(clazz.getDeclaredMethods())) {
            return new InstantiationFallback<>(noArgsConstructor);
        }
        return null;
    }

    private static <T> Constructor<T> getNoArgsConstructor(Class<T> clazz) {
        try {
            // Note ljacqu 20160504: getConstructor(), unlike getDeclaredConstructor(), only considers public members
            return clazz.getConstructor();
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private static <A extends AccessibleObject> boolean isInjectionAnnotationPresent(A[] accessibles) {
        for (A accessible : accessibles) {
            if (accessible.isAnnotationPresent(Inject.class) || accessible.isAnnotationPresent(PostConstruct.class)) {
                return true;
            }
        }
        return false;
    }

}
