package ch.jalu.injector.annotationhandlers;

import ch.jalu.injector.AllInstances;
import ch.jalu.injector.Injector;
import ch.jalu.injector.utils.InjectorUtils;
import org.reflections.Reflections;

import java.util.Set;

/**
 *
 */
public class AllInstancesAnnotationHandler extends TypeSafeAnnotationHandler<AllInstances> {

    private final Injector injector;
    private final Reflections reflections;

    public AllInstancesAnnotationHandler(Injector injector, Reflections reflections) {
        this.injector = injector;
        this.reflections = reflections;
    }

    @Override
    public Class<AllInstances> getAnnotationType() {
        return AllInstances.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] resolveValueSafely(AllInstances annotation) {
        // TODO: Implement detection of cyclic dependencies
        InjectorUtils.checkNotNull(annotation.value(), AllInstances.class);
        Set<Class<?>> subTypes = (Set<Class<?>>) reflections.getSubTypesOf(annotation.value());

        Object[] objects = new Object[subTypes.size()];
        int i = 0;
        for (Class<?> clazz : subTypes) {
            ++i;
            objects[i] = injector.getSingleton(clazz);
        }
        return objects;
    }

}
