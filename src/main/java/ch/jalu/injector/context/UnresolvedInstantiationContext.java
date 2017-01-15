package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context that has not yet been resolved (instantiation method still unknown).
 */
public class UnresolvedInstantiationContext<T> extends InstantiationContext<T> {

    /**
     * Constructor.
     *
     * @param injector the injector
     * @param resolutionType the context in which the object is desired
     * @param clazz the class requested to be instantiated
     */
    public UnresolvedInstantiationContext(Injector injector, ResolutionType resolutionType, Class<T> clazz) {
        super(injector, resolutionType, clazz);
    }

    /**
     * Sets the class to instantiate an object of.
     *
     * @param clazz class to use (child class of requested class)
     */
    public void setMappedClass(Class<? extends T> clazz) {
        if (originalClass.isAssignableFrom(clazz)) {
            mappedClass = clazz;
        } else {
            throw new InjectorException("New mapped class '" + clazz + "' is not a child of original class '"
                + originalClass + "'");
        }
    }

    /**
     * Creates a resolved instantiation context with the provided instantiation method.
     * The instantiation method's type should be equal to the mapped class' type.
     *
     * @param instantiation the instantiation for the mapped class
     * @return resolved instantiation context
     */
    public ResolvedInstantiationContext<T> buildResolvedContext(Instantiation<? extends T> instantiation) {
        return new ResolvedInstantiationContext<>(
            injector, resolutionType, originalClass, mappedClass, instantiation);
    }
}
