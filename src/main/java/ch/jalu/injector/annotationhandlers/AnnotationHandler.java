package ch.jalu.injector.annotationhandlers;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 *
 */
public interface AnnotationHandler {

    @Nullable
    Object resolveValue(Annotation... annotations);

}
