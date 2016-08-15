package ch.jalu.injector.handlers.instantiation;

import java.util.List;

/**
 * Common interface for all instantiation methods.
 *
 * @param <T> the type of the concerned object
 */
public interface Instantiation<T> {

    /**
     * Returns the dependencies that must be provided to instantiate the given item.
     *
     * @return list of dependencies
     * @see #instantiateWith
     */
    List<DependencyDescription> getDependencies();

    /**
     * Creates a new instance with the given values as dependencies. The given values
     * must correspond to {@link #getDependencies()} in size, order and type
     * (as given by {@link DependencyDescription#getType}.
     *
     * @param values the values to set for the dependencies
     * @return resulting object
     */
    T instantiateWith(Object... values);
}
