package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.AllTypes;
import ch.jalu.injector.Injector;
import org.reflections.Reflections;

/**
 * Annotation handler for {@link AllTypes}. Dependencies with this annotation will be
 * assigned a collection of all known subtypes in the project's package.
 */
public class AllTypesAnnotationHandler extends TypeSafeAnnotationHandler<AllTypes> {

    private Reflections reflections;

    public AllTypesAnnotationHandler(String rootPackage) {
        reflections = new Reflections(rootPackage);
    }

    @Override
    public Class<AllTypes> getAnnotationType() {
        return AllTypes.class;
    }

    @Override
    public Object[] resolveValueSafely(Injector injector, Class<?> type, AllTypes annotation) {
        return reflections.getSubTypesOf(annotation.value()).toArray();
    }
}
