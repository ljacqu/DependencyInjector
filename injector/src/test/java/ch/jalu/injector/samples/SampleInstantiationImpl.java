package ch.jalu.injector.samples;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

/**
 * An example for custom implementation of {@link Handler#resolve(ResolutionContext)}
 * and {@link Resolution}. Allows to instantiate classes that have a static {@code create()} method.
 */
public class SampleInstantiationImpl implements Handler {

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        final Class<?> clazz = context.getIdentifier().getTypeAsClass();
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

    private static final class CustomInstantiation<T> implements Resolution<T> {

        private final Method method;

        public CustomInstantiation(Method method) {
            this.method = method;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        @SuppressWarnings("unchecked")
        public T instantiateWith(Object... values) {
            return (T) ReflectionUtils.invokeMethod(method, null);
        }
    }
}
