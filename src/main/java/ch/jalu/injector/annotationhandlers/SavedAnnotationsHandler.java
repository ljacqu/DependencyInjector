package ch.jalu.injector.annotationhandlers;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.InjectorUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class SavedAnnotationsHandler implements AnnotationHandler {

    private Map<Class<?>, Object> storedValues = new HashMap<>();

    @Override
    public Object resolveValue(Annotation... annotations) {
        Object o;
        for (Annotation annotation : annotations) {
            if ((o = storedValues.get(annotation.annotationType())) != null) {
                return o;
            }
        }
        return null;
    }

    public void register(Class<? extends Annotation> annotation, Object object) {
        InjectorUtils.checkNotNull(annotation, "Annotation may not be null", null);
        InjectorUtils.checkNotNull(object, "Object may not be null", annotation);
        if (storedValues.containsKey(annotation)) {
            throw new InjectorException("Value already registered for @" + annotation.getSimpleName(), annotation);
        }
        storedValues.put(annotation, object);
    }
}
