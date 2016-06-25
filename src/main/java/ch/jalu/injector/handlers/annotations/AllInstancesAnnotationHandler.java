package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.AllInstances;
import ch.jalu.injector.Injector;
import ch.jalu.injector.utils.InjectorUtils;
import org.reflections.Reflections;

import javax.inject.Inject;
import java.util.Set;

/**
 * Handler for {@link AllInstances}. Finds all subtypes of the given dependency,
 * instantiates them and assigns the collection to the given depenency.
 */
public class AllInstancesAnnotationHandler extends TypeSafeAnnotationHandler<AllInstances> {

    @Inject
    private Injector injector;
    @Inject
    private String rootPackage;
    private Reflections reflections;

    @Override
    public Class<AllInstances> getAnnotationType() {
        return AllInstances.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object[] resolveValueSafely(Class<?> clazz, AllInstances annotation) {
        // TODO: Implement detection of cyclic dependencies
        InjectorUtils.checkNotNull(annotation.value(), AllInstances.class);
        // Eclipse complains about Set<Class<? extends ?>>, so we need to cast it to Object first. Should be safe.
        Set<Class<?>> subTypes = (Set<Class<?>>) (Object) getReflections().getSubTypesOf(annotation.value());

        Object[] objects = new Object[subTypes.size()];
        int i = 0;
        for (Class<?> subType : subTypes) {
            ++i;
            objects[i] = injector.getSingleton(subType);
        }
        return objects;
    }

    private Reflections getReflections() {
        if (reflections == null) {
            reflections = new Reflections(rootPackage);
        }
        return reflections;
    }

}
