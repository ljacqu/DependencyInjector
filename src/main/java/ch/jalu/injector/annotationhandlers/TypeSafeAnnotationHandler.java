package ch.jalu.injector.annotationhandlers;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * .
 */
public abstract class TypeSafeAnnotationHandler<T extends Annotation> implements AnnotationHandler {

    @Override
    public final Object resolveValue(Annotation... annotations) {
        final Class<T> type = getAnnotationType();
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return resolveValueSafely(type.cast(annotation));
            }
        }
        return null;
    }

    protected abstract Class<T> getAnnotationType();

    @Nullable
    protected abstract Object resolveValueSafely(T annotation);
}
