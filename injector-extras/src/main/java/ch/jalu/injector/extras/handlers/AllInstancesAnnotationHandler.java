package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.context.ResolutionType;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.extras.AllInstances;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;
import org.reflections.Reflections;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    public Resolution<?> resolveValueSafely(ResolutionContext context, AllInstances annotation) {
        // The raw type, e.g. List or array
        final Class<?> rawType = context.getIdentifier().getTypeAsClass();
        // The type of the collection, e.g. String for List<String> or String[]
        final Class genericType = ReflectionUtils.getCollectionType(rawType, context.getIdentifier().getType());

        if (genericType == null) {
            throw new InjectorException("Unsupported dependency of type '" + rawType
                + "' annotated with @AllInstances. (Or did you forget the generic type?)");
        }

        @SuppressWarnings("unchecked")
        Set<Class<?>> subTypes = reflections.getSubTypesOf(genericType);
        ResolutionType resolutionType = context.getIdentifier().getResolutionType();
        List<ObjectIdentifier> dependencies = subTypes.stream()
            .filter(InjectorUtils::canInstantiate)
            .map(clazz -> new ObjectIdentifier(resolutionType, clazz))
            .collect(Collectors.toList());
        return new AllInstancesInstantiation(rawType, dependencies);
    }

    private static final class AllInstancesInstantiation implements Resolution<Object> {

        private final Class<?> rawCollectionType;
        private final List<ObjectIdentifier> dependencies;

        AllInstancesInstantiation(Class<?> rawCollectionType, List<ObjectIdentifier> dependencies) {
            this.rawCollectionType = rawCollectionType;
            this.dependencies = dependencies;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return dependencies;
        }

        @Override
        public Object instantiateWith(Object... values) {
            // TODO: Revise signature -> creating a Set that will potentially be converted back to an array...
            Set<Object> objects = new HashSet<>(Arrays.asList(values));
            return ReflectionUtils.toSuitableCollectionType(rawCollectionType, objects);
        }

        @Override
        public boolean isInstantiation() {
            return true;
        }
    }
}
