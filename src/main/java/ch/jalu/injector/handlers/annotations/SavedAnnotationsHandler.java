package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.InjectorUtils;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple handler that allows you to save a value for a custom annotation. Use this
 * if you have a handful of custom values you want to be able to use as dependency.
 * Create custom annotations to identify them by and register them before using the
 * injector.
 * <p>
 * Don't forget that annotations need to have their {@link java.lang.annotation.Retention retention}
 * set to runtime in order to be visible.
 *
 * @see #register(Class, Object)
 */
public class SavedAnnotationsHandler implements AnnotationHandler {

    private Map<Class<?>, Object> storedValues = new HashMap<>();

    @Override
    public Object resolveValue(Injector injector, Class<?> type, Annotation... annotations) {
        Object o;
        for (Annotation annotation : annotations) {
            if ((o = storedValues.get(annotation.annotationType())) != null) {
                return o;
            }
        }
        return null;
    }

    /**
     * Registers a value for the given annotation.
     *
     * @param annotation the annotation to identify the value by
     * @param object the value to register
     */
    public void register(Class<? extends Annotation> annotation, Object object) {
        InjectorUtils.checkNotNull(annotation, "Annotation may not be null", null);
        InjectorUtils.checkNotNull(object, "Object may not be null", annotation);
        if (storedValues.containsKey(annotation)) {
            throw new InjectorException("Value already registered for @" + annotation.getSimpleName(), annotation);
        }
        storedValues.put(annotation, object);
    }
}
