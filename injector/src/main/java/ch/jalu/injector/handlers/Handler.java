package ch.jalu.injector.handlers;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.instantiation.Resolution;

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
     * instantiated or retrieved. May throw an exception if something is invalid, such as when the requested
     * object is not possible to resolve (e.g. wrong combination of annotations, unmet conditions).
     * <p>
     * The returned {@link Resolution} must correspond to the type represented by the context's object identifier.
     *
     * @param context the resolution context
     * @return the instantiation for the class, or {@code null} if not possible
     * @throws Exception for validation errors
     */
    @Nullable
    default Resolution<?> resolve(ResolutionContext context) throws Exception {
        return null;
    }

    /**
     * Processes the newly created object.
     *
     * @param object the object that was instantiated
     * @param context the resolution context
     * @param resolution the resolution that was used to create the object
     * @param <T> the object's type
     * @return the new object to replace the instance with, null to keep the object the same
     * @throws Exception for validation errors or similar
     */
    @Nullable
    default <T> T postProcess(T object, ResolutionContext context, Resolution<?> resolution) throws Exception {
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
     * @param <T> the class's type
     * @throws Exception for unsuccessful validation, etc.
     */
    default <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) throws Exception {
    }

    /**
     * Processes the given provider class.
     *
     * @param clazz the class to associate the provider class with
     * @param providerClass the provider class
     * @param <T> the class's type
     * @param <P> the provider class's type
     * @throws Exception for unsuccessful validation, etc.
     */
    default <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz,
                                                                      Class<P> providerClass) throws Exception {
    }
}
