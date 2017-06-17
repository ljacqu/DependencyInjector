package ch.jalu.injector.context;

import ch.jalu.injector.Injector;

/**
 * Context of instantiation.
 */
public abstract class ObjectContext {

    protected final Injector injector;
    protected final ResolutionType resolutionType;
    protected final ObjectIdentifier originalIdentifier;
    protected ObjectIdentifier identifier;

    public ObjectContext(Injector injector, ResolutionType resolutionType, ObjectIdentifier objectIdentifier) {
        this(injector, resolutionType, objectIdentifier, objectIdentifier);
    }

    public ObjectContext(Injector injector, ResolutionType resolutionType, ObjectIdentifier originalIdentifier,
                         ObjectIdentifier identifier) {
        this.injector = injector;
        this.resolutionType = resolutionType;
        this.originalIdentifier = originalIdentifier;
        this.identifier = identifier;
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

    public ObjectIdentifier getOriginalIdentifier() {
        return originalIdentifier;
    }

    public ObjectIdentifier getIdentifier() {
        return identifier;
    }
}
