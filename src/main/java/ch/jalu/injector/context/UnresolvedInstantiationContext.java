package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context that has not yet been resolved.
 */
public class UnresolvedInstantiationContext<T> extends InstantiationContext<T> {

    public UnresolvedInstantiationContext(Injector injector, Class<T> clazz, ResolutionType resolutionType) {
        super(injector, resolutionType, clazz);
    }

    public void setMappedClass(Class<? extends T> clazz) {
        if (originalClass.isAssignableFrom(clazz)) {
            mappedClass = clazz;
        } else {
            throw new InjectorException("New mapped class '" + clazz + "' is not a child of original class '"
                + originalClass + "'");
        }
    }

    public ResolvedInstantiationContext<T> buildResolvedContext(Instantiation<? extends T> instantiation) {
        return new ResolvedInstantiationContext<>(
            injector, resolutionType, originalClass, mappedClass, instantiation);
    }
}
