package ch.jalu.injector.handlers;

import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;

/**
 * Handlers may modify the injection process at different moments.
 * They allow you to define custom injection methods, custom validation.
 *
 * Handlers are executed in the order that they are given to {@link ch.jalu.injector.InjectorBuilder}, so more important
 * handlers should come first.
 */
public interface Handler {

    /**
     * Resolves the context such that the object identified by the context's object identifier can be
     * instantiated or retrieved. May throw an exception e.g. if annotations aren't used correctly.
     *
     * @param context the instantiation context
     * @return the instantiation for the class, or {@code null} if not possible
     * @throws Exception for validation errors
     */
    @Nullable
    default Instantiation<?> get(UnresolvedContext context) throws Exception {
        return null;
    }

    /**
     * Processes the newly created object.
     *
     * @param object the object that was instantiated
     * @param context the instantiation context
     * @param <T> the object's type
     * @return the new object to replace the instance with, null to keep the object the same
     * @throws Exception for validation errors or similar
     */
    @Nullable
    default <T> T postProcess(T object, ResolvedContext context) throws Exception {
        return null;
    }

    /**
     * Processes the annotation type and the associated object.
     *
     * @param annotationType the annotation type
     * @param object the object
     * @throws Exception for failed validations
     */
    default void onAnnotation(Class<? extends Annotation> annotationType, @Nullable Object object) throws Exception {
    }

    /**
     * Processes the given provider.
     *
     * @param clazz the class to associate the provider with
     * @param provider the provider
     * @param <T> the class' type
     * @throws Exception for unsuccessful validation, etc.
     */
    default <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) throws Exception {
    }

    /**
     * Processes the given provider class.
     *
     * @param clazz the class to associate the provider class with
     * @param providerClass the provider class
     * @param <T> the class' type
     * @param <P> the provider class' type
     * @throws Exception for unsuccessful validation, etc.
     */
    default <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz,
                                                                      Class<P> providerClass) throws Exception {
    }
}
