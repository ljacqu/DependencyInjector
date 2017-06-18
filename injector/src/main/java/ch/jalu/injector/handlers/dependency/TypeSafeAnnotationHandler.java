package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Type safe annotation handler base, which will fire the resolve method only if
 * an annotation of the given type is present.
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements Handler {

    @Override
    public final Resolution<?> resolve(ResolutionContext context) throws Exception {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : context.getIdentifier().getAnnotations()) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(context, type.cast(annotation));
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
     * @return the resolved value, or null if none applicable
     * @throws Exception for invalid usage of annotation
     */
    @Nullable
    protected abstract Resolution<?> resolveValueSafely(ResolutionContext context, T annotation) throws Exception;
}