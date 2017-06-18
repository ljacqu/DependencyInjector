package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;

import java.util.HashMap;
import java.util.Map;

/**
 * Pre construct handler that maps abstract classes to a previously registered implementation.
 */
public class ImplementationClassHandler extends AbstractCountingHandler implements Handler {

    private Map<Class<?>, Class<?>> classMap = new HashMap<>();

    /**
     * Registers a child class to use when the parent is requested to be instantiated.
     *
     * @param parent the parent class to "redirect"
     * @param child the child class to use
     * @param <T> the parent's type
     */
    public <T> void register(Class<T> parent, Class<? extends T> child) {
        classMap.put(parent, child);
    }

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        increment(context);
        Class<?> implClass = getImplClass(context.getIdentifier().getTypeAsClass());
        if (implClass != null) {
            context.setIdentifier(new ObjectIdentifier(context.getIdentifier().getResolutionType(), implClass));
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<? extends T> getImplClass(Class<T> clazz) {
        Class<? extends T> child = (Class<? extends T>) classMap.get(clazz);
        if (child == null) {
            return clazz;
        }
        return getImplClass(child);
    }

    @Override
    public <T> T postProcess(T object, ResolutionContext context, Resolution<?> resolution) {
        // Injector doesn't register the object with the mapped class by default. In this test case, this is desirable.
        final Injector injector = context.getInjector();
        final Class<?> mappedClass = context.getIdentifier().getTypeAsClass();
        if (mappedClass != context.getOriginalIdentifier().getTypeAsClass() && injector.getIfAvailable(mappedClass) == null) {
            injector.register((Class) mappedClass, object);
        }
        return null;
    }
}
