package ch.jalu.injector.samples;

import ch.jalu.injector.context.UnresolvedInstantiationContext;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * An example for custom implementation of {@link InstantiationProvider} and {@link Instantiation}.
 * Allows to instantiate classes that have a static {@code create()} method.
 */
public class SampleInstantiationImpl implements InstantiationProvider {

    @Override
    public <T> CustomInstantiation<T> get(UnresolvedInstantiationContext<T> context) {
        final Class<?> clazz = context.getMappedClass();
        try {
            Method method = clazz.getDeclaredMethod("create");
            if (Modifier.isStatic(method.getModifiers()) && clazz.isAssignableFrom(method.getReturnType())) {
                method.setAccessible(true);
                return new CustomInstantiation<>(method);
            }
        } catch (NoSuchMethodException e) {
            // No such method -- instantiation not possible with this method
        }
        return null;
    }

    private static final class CustomInstantiation<T> implements Instantiation<T> {

        private final Method method;

        public CustomInstantiation(Method method) {
            this.method = method;
        }

        @Override
        public List<DependencyDescription> getDependencies() {
            return new ArrayList<>();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T instantiateWith(Object... values) {
            return (T) ReflectionUtils.invokeMethod(method, null);
        }
    }
}
