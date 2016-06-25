package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.AllTypes;
import org.reflections.Reflections;

import javax.inject.Inject;

/**
 * Annotation handler for {@link AllTypes}. Dependencies with this annotation will be
 * assigned a collection of all known subtypes in the project's package.
 */
public class AllTypesAnnotationHandler extends TypeSafeAnnotationHandler<AllTypes> {

    @Inject
    private String rootPackage;
    private Reflections reflections;

    @Override
    public Class<AllTypes> getAnnotationType() {
        return AllTypes.class;
    }

    @Override
    public Object[] resolveValueSafely(Class<?> type, AllTypes annotation) {
        return getReflections().getSubTypesOf(annotation.value()).toArray();
    }

    private Reflections getReflections() {
        if (reflections == null) {
            reflections = new Reflections(rootPackage);
        }
        return reflections;
    }
}
