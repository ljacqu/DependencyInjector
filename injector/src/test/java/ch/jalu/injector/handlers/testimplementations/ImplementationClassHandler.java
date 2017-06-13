package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.context.UnresolvedInstantiationContext;
import ch.jalu.injector.handlers.Handler;

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
    public <T> void preProcess(UnresolvedInstantiationContext<T> context) {
        increment();
        Class<? extends T> implClass = getImplClass(context.getMappedClass());
        if (implClass != null) {
            context.setMappedClass(implClass);
        }
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
    public <T> T postProcess(T object, ResolvedInstantiationContext<T> context) {
        // Injector doesn't register the object with the mapped class by default. In this test case, this is desirable.
        Injector injector = context.getInjector();
        if (context.getMappedClass() != context.getOriginalClass()
                && injector.getIfAvailable(context.getMappedClass()) == null) {
            injector.register((Class) context.getMappedClass(), object);
        }
        return null;
    }
}
