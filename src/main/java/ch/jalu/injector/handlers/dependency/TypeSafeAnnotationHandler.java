package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.instantiation.DependencyDescription;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Type safe annotation handler base, which will fire the resolve method only if
 * an annotation of the given type is present.
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements DependencyHandler {

    @Override
    public final Object resolveValue(Injector injector, DependencyDescription dependencyDescription) throws Exception {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : dependencyDescription.getAnnotations()) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(injector, type.cast(annotation), dependencyDescription);
            }
        }
        return null;
    }

    /**
     * Returns the class of the annotation the handler can process.
     *
     * @return the annotation type
     */
    protected abstract Class<T> getAnnotationType();

    /**
     * Resolves the value with the matched annotation, guaranteed to never be null.
     *
     * @param injector the injector
     * @param annotation the matched annotation
     * @param dependencyDescription the entire dependency description
     * @return the resolved value, or null if none applicable
     */
    @Nullable
    protected abstract Object resolveValueSafely(Injector injector, T annotation,
                                                 DependencyDescription dependencyDescription) throws Exception;
}