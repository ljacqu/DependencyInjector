package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;

/**
 * Resolution context: contains data about the object that is requested, such as identifying
 * information about the object to retrieve or construct and the context in which it is being
 * requested.
 */
public class ResolutionContext {

    private final Injector injector;
    private final ObjectIdentifier originalIdentifier;
    private ObjectIdentifier identifier;

    public ResolutionContext(Injector injector, ObjectIdentifier identifier) {
        this.injector = injector;
        this.originalIdentifier = identifier;
        this.identifier = identifier;
    }

    /**
     * @return the injector
     */
    public Injector getInjector() {
        return injector;
    }

    public ObjectIdentifier getOriginalIdentifier() {
        return originalIdentifier;
    }

    public ObjectIdentifier getIdentifier() {
        return identifier;
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
}
