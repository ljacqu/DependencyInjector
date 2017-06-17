package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Type safe annotation handler base, which will fire the resolve method only if
 * an annotation of the given type is present.
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements Handler {

    @Override
    public final Object resolveValue(ResolvedContext context,
                                     DependencyDescription dependencyDescription) throws Exception {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : dependencyDescription.getAnnotations()) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(context, type.cast(annotation), dependencyDescription);
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
     * @param context the instantiation context
     * @param annotation the matched annotation
     * @param dependencyDescription the entire dependency description
     * @return the resolved value, or null if none applicable
     * @throws Exception for invalid usage of annotation
     */
    @Nullable
    protected abstract Object resolveValueSafely(ResolvedContext context, T annotation,
                                                 DependencyDescription dependencyDescription) throws Exception;
}