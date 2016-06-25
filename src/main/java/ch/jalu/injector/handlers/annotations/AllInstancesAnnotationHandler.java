package ch.jalu.injector.handlers.annotations;

import ch.jalu.injector.AllInstances;
import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.instantiation.DependencyDescription;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler for {@link AllInstances}. Finds all subtypes of the given dependency,
 * instantiates them and assigns the collection to the given depenency.
 */
public class AllInstancesAnnotationHandler extends TypeSafeAnnotationHandler<AllInstances> {

    private Reflections reflections;

    public AllInstancesAnnotationHandler(String rootPackage) {
        reflections = new Reflections(rootPackage);
    }

    @Override
    public Class<AllInstances> getAnnotationType() {
        return AllInstances.class;
    }

    @Override
    public Object resolveValueSafely(Injector injector, AllInstances annotation,
                                       DependencyDescription dependencyDescription) {
        // The raw type, e.g. List or array
        final Class<?> rawType = dependencyDescription.getType();
        // The type of the collection, e.g. String for List<String> or String[]
        final Class<?> genericType = ReflectionUtils.getGenericClass(rawType, dependencyDescription.getGenericType());

        if (genericType == null) {
            throw new InjectorException("Unsupported dependency of type '" + rawType
                + "' annotated with @AllInstances. (Or did you forget the generic type?)", rawType);
        }

        // TODO: Implement detection of cyclic dependencies
        // Eclipse complains about Set<Class<? extends ?>>, so we need to cast it to Object first. Should be safe.
        @SuppressWarnings("unchecked")
        Set<Class<?>> subTypes = (Set<Class<?>>) (Object) reflections.getSubTypesOf(genericType);
        Set<Object> instances = new HashSet<>(subTypes.size());

        for (Class<?> type : subTypes) {
            if (InjectorUtils.canInstantiate(type)) {
                instances.add(injector.getSingleton(type));
            }
        }
        return ReflectionUtils.toSuitableCollectionType(rawType, instances);
    }

}
