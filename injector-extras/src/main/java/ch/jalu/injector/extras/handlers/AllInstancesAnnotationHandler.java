package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.extras.AllInstances;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import ch.jalu.injector.handlers.instantiation.Instantiation;
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
    public Instantiation<?> resolveValueSafely(UnresolvedContext context, AllInstances annotation) {
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
        subTypes.removeIf(type -> !InjectorUtils.canInstantiate(type));
        return new AllInstancesInstantiation(rawType, subTypes);
    }

    private static final class AllInstancesInstantiation implements Instantiation<Object> {

        private final Class<?> rawCollectionType;
        private final Set<Class<?>> subtypes;

        AllInstancesInstantiation(Class<?> rawCollectionType, Set<Class<?>> subtypes) {
            this.rawCollectionType = rawCollectionType;
            this.subtypes = subtypes;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return subtypes.stream().map(type -> new ObjectIdentifier(type)).collect(Collectors.toList());
        }

        @Override
        public Object instantiateWith(Object... values) {
            Set<Object> objects = new HashSet<>(Arrays.asList(values));
            return ReflectionUtils.toSuitableCollectionType(rawCollectionType, objects);
        }

        @Override
        public boolean saveIfSingleton() {
            return true;
        }
    }
}
