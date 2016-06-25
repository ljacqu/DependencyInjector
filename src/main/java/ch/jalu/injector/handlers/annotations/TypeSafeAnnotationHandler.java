package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.Injector;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Type safe annotation handler base, which will fire the resolve method only if
 * an annotation of the given type is present.
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements AnnotationHandler {

    @Override
    public final Object resolveValue(Injector injector, Class<?> clazz, Annotation... annotations) throws Exception {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(injector, clazz, type.cast(annotation));
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
     * @param clazz the dependency's type
     * @param annotation the matched annotation
     * @return the resolved value, or null if none applicable
     */
    @Nullable
    protected abstract Object resolveValueSafely(Injector injector, Class<?> clazz, T annotation) throws Exception;
}