package ch.jalu.injector.instantiation;

import java.lang.annotation.Annotation;

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
    Class<?>[] getDependencies();

    /**
     * Returns the annotations on each dependency. The first dimension indices of this
     * array correspond to the ones of {@link #getDependencies()}. Each subarray contains
     * all annotations on the given dependency; empty array if no annotations are present.
     *
     * @return annotation for each dependency
     */
    Annotation[][] getDependencyAnnotations();

    /**
     * Creates a new instance with the given values as dependencies. The given values
     * must correspond to {@link #getDependencies()} in size, order and type.
     *
     * @param values the values to set for the dependencies
     * @return resulting object
     */
    T instantiateWith(Object... values);
}
