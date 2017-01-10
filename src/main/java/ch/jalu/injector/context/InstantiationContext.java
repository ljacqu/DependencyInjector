package ch.jalu.injector.context;

import ch.jalu.injector.Injector;

/**
 * Context of instantiation.
 */
public abstract class InstantiationContext<T> {

    protected final Injector injector;
    protected final ResolutionType resolutionType;
    protected final Class<T> originalClass;
    protected Class<? extends T> mappedClass;

    public InstantiationContext(Injector injector, ResolutionType resolutionType, Class<T> originalClass) {
        this(injector, resolutionType, originalClass, originalClass);
    }

    public InstantiationContext(Injector injector, ResolutionType resolutionType, Class<T> originalClass,
                                Class<? extends T> mappedClass) {
        this.injector = injector;
        this.originalClass = originalClass;
        this.resolutionType = resolutionType;
        this.mappedClass = mappedClass;
    }

    /**
     * @return the injector
     */
    public Injector getInjector() {
        return injector;
    }

    /**
     * @return the context in which the object should be instantiated
     */
    public ResolutionType getResolutionType() {
        return resolutionType;
    }

    /**
     * @return the class that was originally requested (may differ from the mapped class)
     */
    public Class<T> getOriginalClass() {
        return originalClass;
    }

    /**
     * @return the class that should be instantiated
     */
    public Class<? extends T> getMappedClass() {
        return mappedClass;
    }
}
