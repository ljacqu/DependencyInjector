package ch.jalu.injector;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * Dependency injector.
 * <p>
 * Allows you to retrieve singletons and new instances. By default, it supports field and constructor injection
 * and executes methods annotated with {@code @PostConstruct}. You can obtain an injector and customize its behavior
 * with the {@link InjectorBuilder}.
 */
public interface Injector {

    /**
     * Registers an object as instance of the given class.
     *
     * @param clazz the class to register the object for
     * @param object the object
     * @param <T> the type to register the object for
     * @since 0.1
     */
    <T> void register(Class<? super T> clazz, T object);

    <T> void registerProvider(Class<T> clazz, Provider<? extends T> provider);

    <T, P extends Provider<? extends T>> void registerProvider(Class<T> clazz, Class<P> providerClass);

    /**
     * Processes an annotation with an associated object. The actual behavior of this method depends on the
     * configured annotation handlers which are added to the injector.
     *
     * @param annotation the annotation
     * @param object the object
     */
    void provide(Class<? extends Annotation> annotation, @Nullable Object object);

    /**
     * Retrieves or instantiates an object of the given type (singleton scope).
     *
     * @param clazz the class to retrieve the value for
     * @param <T> the class' type
     * @return object of the class' type
     * @since 0.1
     */
    <T> T getSingleton(Class<T> clazz);

    /**
     * Request-scoped method to instantiate a new object of the given class. The injector does <i>not</i> keep track
     * of it afterwards; it will always return a new instance and forget about it.
     *
     * @param clazz the class to instantiate
     * @param <T> the class' type
     * @return new instance of class T
     * @since 0.1
     */
    <T> T newInstance(Class<T> clazz);

    /**
     * Returns an instance of the given class if available. This simply returns the instance if present and
     * otherwise {@code null}. Calling this method will not instantiate anything.
     *
     * @param clazz the class to retrieve the instance for
     * @param <T> the class' type
     * @return instance or null if none available
     * @since 0.1
     */
    @Nullable
    <T> T getIfAvailable(Class<T> clazz);

    /**
     * Returns all known singletons of the given type. Typically used
     * with interfaces in order to perform an action without knowing its concrete implementors.
     * Trivially, using {@link Object} as {@code clazz} will return all known singletons.
     *
     * @param clazz the class to retrieve singletons of
     * @param <T> the class' type
     * @return list of singletons of the given type
     * @since 0.1
     */
    <T> Collection<T> retrieveAllOfType(Class<T> clazz);

}