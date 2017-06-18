package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectIdentifier;

import java.util.List;

/**
 * An object resolution knows how to provide the requested object. Some resolutions simply wrap an
 * existing value (e.g. if a requested singleton has already been created), others may define
 * {@link #getDependencies() dependencies} which must be provided so that it can create the object.
 *
 * @param <T> the type of the requested object
 */
public interface Resolution<T> {

    /**
     * Returns the dependencies that must be provided to create or retrieve the requested object.
     *
     * @return list of dependencies
     * @see #instantiateWith
     */
    List<ObjectIdentifier> getDependencies();

    /**
     * Creates or retrieves an object with the given values as dependencies. The given values
     * must correspond to {@link #getDependencies()} in size, order and type
     * (as given by {@link ObjectIdentifier#getType}.
     *
     * @param values the required dependencies
     * @return the resulting object
     */
    T instantiateWith(Object... values);

    /**
     * Returns whether this resolution will instantiate an object or not.
     * Certain resolutions simply need to retrieve an existing object; this method
     * returns {@code true} when the requested object has to be instantiated.
     *
     * @return true if a newly created object will be returned, false if the object already exists
     */
    default boolean isInstantiation() {
        return false;
    }
}
