package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context after the class has been resolved, i.e. it is known what
 * instantiation method is used for it.
 */
public class ResolvedInstantiationContext<T> extends InstantiationContext<T> {

    private final Instantiation<? extends T> instantiation;

    /**
     * Constructor.
     * <p>
     * The type of {@link #mappedClass} and {@link #instantiation} should be identical.
     *
     * @param injector the injector
     * @param resolutionType the resolution type
     * @param originalClass the class that was originally requested
     * @param mappedClass the class that was mapped to be instantiated
     * @param instantiation the instantiation method
     */
    public ResolvedInstantiationContext(Injector injector, ResolutionType resolutionType, Class<T> originalClass,
                                        Class<? extends T> mappedClass, Instantiation<? extends T> instantiation) {
        super(injector, resolutionType, originalClass, mappedClass);
        this.instantiation = instantiation;
    }

    /**
     * @return the instantiation method with which the object should be created
     */
    public Instantiation<? extends T> getInstantiation() {
        return instantiation;
    }
}
