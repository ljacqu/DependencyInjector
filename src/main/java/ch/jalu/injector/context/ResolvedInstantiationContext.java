package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context after the class has been resolved.
 */
public class ResolvedInstantiationContext<T> extends InstantiationContext<T> {

    private final Instantiation<? extends T> instantiation;

    public ResolvedInstantiationContext(Injector injector, ResolutionType resolutionType, Class<T> originalClass,
                                        Class<? extends T> mappedClass, Instantiation<? extends T> instantiation) {
        super(injector, resolutionType, originalClass, mappedClass);
        this.instantiation = instantiation;
    }

    public Instantiation<? extends T> getInstantiation() {
        return instantiation;
    }
}
