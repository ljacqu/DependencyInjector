package ch.jalu.injector.handlers.annotationvalues;

import ch.jalu.injector.handlers.Handler;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;

/**
 * Handlers for (Annotation type, Object) pairs registered via {@link ch.jalu.injector.Injector#provide(Class, Object)}.
 * This can be used to provide external dependencies that are identified by a custom annotation
 * (see {@link ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler}).
 */
public interface AnnotationValueHandler extends Handler {

    /**
     * Processes the annotation type and the associated object.
     *
     * @param annotationType the annotation type
     * @param object the object
     * @throws Exception for failed validations
     */
    void processProvided(Class<? extends Annotation> annotationType, @Nullable Object object) throws Exception;

}
