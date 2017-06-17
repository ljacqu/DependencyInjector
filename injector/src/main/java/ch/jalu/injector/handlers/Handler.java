package ch.jalu.injector.handlers;

import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
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
     * Processes an incoming request for instantiation for validation or custom mapping.
     *
     * @param context the instantiation context
     * @throws Exception for failed validation or preconditions
     */
    default void preProcess(UnresolvedContext context) throws Exception {
    }

    /**
     * Provides an instantiation method for the given class if available.
     *
     * @param context the instantiation context
     * @return the instantiation for the class, or {@code null} if not possible
     */
    @Nullable
    default Instantiation<?> get(UnresolvedContext context) {
        return null;
    }

    /**
     * Resolves the value of a dependency based on the present annotations and the declared type.
     * Returns {@code null} if the given annotations and field type do not apply
     * to the handler. May throw an exception if a given annotation is being used wrong.
     * <p>
     * Note that you are you not forced to check if the returned Object is valid for the given
     * dependency {@code type}, unless you want to show a specific error message.
     *
     * @param context instantiation context
     * @param dependencyDescription description of the dependency
     * @return the resolved value, or null if not applicable
     * @throws Exception for invalid usage of annotation
     */
    @Nullable
    default Object resolveValue(ResolvedContext context,
                                DependencyDescription dependencyDescription) throws Exception {
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
