package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context after the class has been resolved, i.e. it is known what
 * instantiation method is used for it.
 */
public class ResolvedContext extends ObjectContext {

    private final Instantiation<?> instantiation;

    /**
     * Constructor.
     * <p>
     * The type of {@link #identifier} and {@link #instantiation} should be identical.
     *
     * @param injector the injector
     * @param resolutionType the resolution type
     * @param originalIdentifier the class that was originally requested
     * @param identifier the class that was mapped to be instantiated
     * @param instantiation the instantiation method
     */
    public ResolvedContext(Injector injector, ResolutionType resolutionType, ObjectIdentifier originalIdentifier,
                           ObjectIdentifier identifier, Instantiation<?> instantiation) {
        super(injector, resolutionType, originalIdentifier, identifier);
        this.instantiation = instantiation;
    }

    /**
     * @return the instantiation method with which the object should be created
     */
    public Instantiation<?> getInstantiation() {
        return instantiation;
    }
}
