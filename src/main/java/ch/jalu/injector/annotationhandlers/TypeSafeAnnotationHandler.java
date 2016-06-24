package ch.jalu.injector.annotationhandlers;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Type safe annotation handler base, which will fire the resolve method only if
 * an annotation of the given type is present.
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements AnnotationHandler {

    @Override
    public final Object resolveValue(Class<?> clazz, Annotation... annotations) throws Exception {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(clazz, type.cast(annotation));
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
     * @param clazz the dependency's type
     * @param annotation the matched annotation
     * @return the resolved value, or null if none applicable
     */
    @Nullable
    protected abstract Object resolveValueSafely(Class<?> clazz, T annotation) throws Exception;
}