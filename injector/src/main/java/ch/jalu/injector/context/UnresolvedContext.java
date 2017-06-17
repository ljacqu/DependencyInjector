package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Instantiation context that has not yet been resolved (instantiation method still unknown).
 */
public class UnresolvedContext extends ObjectContext {

    /**
     * Constructor.
     *
     * @param injector the injector
     * @param resolutionType the context in which the object is desired
     * @param identifier the class requested to be instantiated
     */
    public UnresolvedContext(Injector injector, ResolutionType resolutionType, ObjectIdentifier identifier) {
        super(injector, resolutionType, identifier);
    }

    /**
     * Sets the class to instantiate an object of.
     *
     * @param identifier class to use (child class of requested class)
     */
    public void setIdentifier(ObjectIdentifier identifier) {
        if (originalIdentifier.getTypeAsClass().isAssignableFrom(identifier.getTypeAsClass())) {
            this.identifier = identifier;
        } else {
            throw new InjectorException("New mapped class '" + identifier.getTypeAsClass()
                + "' is not a child of original class '" + originalIdentifier.getTypeAsClass() + "'");
        }
    }

    /**
     * Creates a resolved instantiation context with the provided instantiation method.
     * The instantiation method's type should be equal to the mapped class' type.
     *
     * @param instantiation the instantiation for the mapped class
     * @return resolved instantiation context
     */
    public ResolvedContext buildResolvedContext(Instantiation<?> instantiation) {
        return new ResolvedContext(
            injector, resolutionType, originalIdentifier, identifier, instantiation);
    }
}
