package ch.jalu.injector;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * The injector interface.
 */
public interface Injector {

    /**
     * Register an object as instance of the given class.
     *
     * @param clazz the class to register the object for
     * @param object the object
     * @param <T> the type to register the object for
     */
    <T> void register(Class<? super T> clazz, T object);

    /**
     * Register a value that is identified by an annotation.
     *
     * @param annotation the annotation
     * @param value the value
     */
    void provide(Class<? extends Annotation> annotation, Object value);

    /**
     * Retrieves or instantiates an object of the given type (singleton scope).
     *
     * @param clazz the class to retrieve the value for
     * @param <T> the class' type
     * @return object of the class' type
     */
    <T> T getSingleton(Class<T> clazz);

    /**
     * Request-scoped method to instantiate a new object of the given class. The injector does <i>not</i> keep track
     * of it afterwards; it will always return a new instance and forget about it.
     *
     * @param clazz the class to instantiate
     * @param <T> the class' type
     * @return new instance of class T
     */
    <T> T newInstance(Class<T> clazz);

    /**
     * Returns an instance of the given class if available. This simply returns the instance if present and
     * otherwise {@code null}. Calling this method will not instantiate anything.
     *
     * @param clazz the class to retrieve the instance for
     * @param <T> the class' type
     * @return instance or null if none available
     */
    @Nullable
    <T> T getIfAvailable(Class<T> clazz);

    /**
     * Returns the singleton for the given class if available, and all singletons of children types. Typically used
     * with interfaces in order to perform an action without knowing its concrete implementors.
     * Trivially, using {@link Object} as {@code clazz} will return all known singletons.
     *
     * @param clazz the class to retrieve
     * @param <T> the class' type
     * @return list of singletons of the given type
     */
    <T> Collection<T> getSingletonsOfType(Class<T> clazz);

}