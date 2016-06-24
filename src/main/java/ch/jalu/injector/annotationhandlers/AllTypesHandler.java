package ch.jalu.injector.annotationhandlers;

import ch.jalu.injector.AllTypes;
import org.reflections.Reflections;

/**
 *
 */
public class AllTypesHandler extends TypeSafeAnnotationHandler<AllTypes> {

    private Reflections reflections;

    public AllTypesHandler(Reflections reflections) {
        this.reflections = reflections;
    }

    @Override
    public Class<AllTypes> getAnnotationType() {
        return AllTypes.class;
    }

    @Override
    public Object[] resolveValueSafely(AllTypes annotation) {
        return reflections.getSubTypesOf(annotation.value()).toArray();
    }
}
