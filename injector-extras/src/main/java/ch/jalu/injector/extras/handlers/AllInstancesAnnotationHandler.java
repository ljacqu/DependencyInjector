package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.extras.AllInstances;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;
import org.reflections.Reflections;

import java.util.HashSet;
import java.util.Set;

/**
 * Handler for {@link AllInstances}. Finds all subtypes of the given dependency,
 * instantiates them and assigns the collection to the given dependency.
 * <p>
 * Requires that you add the <a href="https://github.com/ronmamo/reflections">reflections project</a>
 * as dependency to be able to use this.
 */
public class AllInstancesAnnotationHandler extends TypeSafeAnnotationHandler<AllInstances> {

    private Reflections reflections;

    public AllInstancesAnnotationHandler(String rootPackage) {
        reflections = new Reflections(rootPackage);
    }

    @Override
    protected Class<AllInstances> getAnnotationType() {
        return AllInstances.class;
    }

    @Override
    public Object resolveValueSafely(ResolvedInstantiationContext<?> context, AllInstances annotation,
                                     DependencyDescription dependencyDescription) {
        // The raw type, e.g. List or array
        final Class<?> rawType = dependencyDescription.getTypeAsClass();
        // The type of the collection, e.g. String for List<String> or String[]
        final Class genericType = ReflectionUtils.getCollectionType(rawType, dependencyDescription.getType());

        if (genericType == null) {
            throw new InjectorException("Unsupported dependency of type '" + rawType
                + "' annotated with @AllInstances. (Or did you forget the generic type?)");
        }

        // TODO: Implement detection of cyclic dependencies
        @SuppressWarnings("unchecked")
        Set<Class<?>> subTypes = reflections.getSubTypesOf(genericType);
        Set<Object> instances = new HashSet<>(subTypes.size());

        final Injector injector = context.getInjector();
        for (Class<?> type : subTypes) {
            if (InjectorUtils.canInstantiate(type)) {
                instances.add(injector.getSingleton(type));
            }
        }
        return ReflectionUtils.toSuitableCollectionType(rawType, instances);
    }

}
