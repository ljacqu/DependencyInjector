package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Resolution context: contains data about the object that is requested, such as identifying
 * information about the object to retrieve or construct and the context in which it is being
 * requested.
 */
public class ResolutionContext {

    private final Injector injector;
    private final ObjectIdentifier originalIdentifier;
    private ObjectIdentifier identifier;
    private List<ResolutionContext> parents = new ArrayList<>();

    /**
     * Creates a new resolution context with no predecessors.
     *
     * @param injector the injector
     * @param identifier the identifier of the object to create
     */
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

    public List<ResolutionContext> getParents() {
        return parents;
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
     * Creates a context for the given identifier with this context as parent.
     *
     * @param identifier the identifier to create a context for
     * @return the child context
     */
    public ResolutionContext createChildContext(ObjectIdentifier identifier) {
        ResolutionContext child = new ResolutionContext(injector, identifier);
        child.parents.addAll(this.parents);
        child.parents.add(this);
        return child;
    }
}
