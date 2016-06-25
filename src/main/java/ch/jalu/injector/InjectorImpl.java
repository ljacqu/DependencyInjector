package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.instantiation.DependencyDescription;
import ch.jalu.injector.instantiation.Instantiation;
import ch.jalu.injector.utils.InjectorUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.jalu.injector.utils.InjectorUtils.firstNotNull;

/**
 * Dependency injector implementation: initializes and injects classes.
 * <p>
 * Only constructor and field injection are supported, indicated with the JSR330
 * {@link javax.inject.Inject @Inject} annotation.
 * <p>
 * {@link PostConstruct @PostConstruct} methods are recognized and invoked upon
 * instantiation. Note that the parent classes are <i>not</i> scanned for such methods.
 */
public class InjectorImpl implements Injector {

    protected final Map<Class<?>, Object> objects;
    protected InjectorConfig config;

    /**
     * Constructor.
     *
     * @param config injector configuration
     * @see InjectorBuilder
     */
    protected InjectorImpl(InjectorConfig config) {
        this.config = config;
        objects = new HashMap<>();
        objects.put(Injector.class, this);
    }

    @Override
    public <T> T getSingleton(Class<T> clazz) {
        return get(clazz, new HashSet<Class<?>>());
    }

    @Override
    public <T> void register(Class<? super T> clazz, T object) {
        if (objects.containsKey(clazz)) {
            throw new InjectorException("There is already an object present for " + clazz, clazz);
        }
        InjectorUtils.checkNotNull(object, clazz);
        objects.put(clazz, object);
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        return instantiate(clazz, new HashSet<Class<?>>());
    }

    @Override
    public <T> T getIfAvailable(Class<T> clazz) {
        return clazz.cast(objects.get(clazz));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Collection<T> retrieveAll(Class<T> clazz) {
        List<T> instances = new ArrayList<>();
        for (Object object : objects.values()) {
            if (clazz.isInstance(object)) {
                instances.add((T) object);
            }
        }
        return instances;
    }

    /**
     * Returns an instance of the given class by retrieving it or by instantiating it if not yet present.
     *
     * @param clazz the class to retrieve the singleton instance for
     * @param traversedClasses the list of traversed classes
     * @param <T> the class' type
     * @return instance or associated value (for annotations)
     */
    private <T> T get(Class<T> clazz, Set<Class<?>> traversedClasses) {
        if (objects.containsKey(clazz)) {
            return clazz.cast(objects.get(clazz));
        }

        // First time we come across clazz, need to instantiate it. Validate that we can do so
        Class<? extends T> mappedClass = clazz;
        for (PreConstructHandler preConstructHandler : config.getPreConstructHandlers()) {
            try {
                mappedClass = firstNotNull(preConstructHandler.accept(mappedClass), mappedClass);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }

        if (!InjectorUtils.canInstantiate(mappedClass)) {
            throw new InjectorException("Class " + clazz.getSimpleName() + " cannot be instantiated", clazz);
        }

        // Add the clazz to the list of traversed classes in a new Set, so each path we take has its own Set.
        traversedClasses = new HashSet<>(traversedClasses);
        traversedClasses.add(mappedClass);
        T object = instantiate(mappedClass, traversedClasses);
        storeObject(object);
        return object;
    }

    /**
     * Instantiates the given class by locating its @Inject elements and retrieving
     * or instantiating the required instances.
     *
     * @param clazz the class to instantiate
     * @param traversedClasses collection of classes already traversed
     * @param <T> the class' type
     * @return the instantiated object
     */
    private <T> T instantiate(Class<T> clazz, Set<Class<?>> traversedClasses) {
        Instantiation<T> instantiation = InjectionHelper.getInjection(clazz);
        if (instantiation == null) {
            throw new InjectorException("Did not find injection method for " + clazz + ". Make sure you have "
                + "a constructor with @Inject or fields with @Inject. Fields with @Inject require "
                + "the default constructor", clazz);
        }

        validateInjectionHasNoCircularDependencies(instantiation.getDependencies(), traversedClasses);
        Object[] dependencies = resolveDependencies(instantiation, traversedClasses);
        T object = instantiation.instantiateWith(dependencies);
        for (PostConstructHandler postConstructHandler : config.getPostConstructHandlers()) {
            try {
                postConstructHandler.process(object);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }

        return object;
    }

    /**
     * Resolves the dependencies for the given class instantiation, i.e. returns a collection that satisfy
     * the class' dependencies by retrieving elements or instantiating them where necessary.
     *
     * @param instantiation the injection parameters
     * @param traversedClasses collection of traversed classes
     * @return array with the parameters to use in the constructor
     */
    private Object[] resolveDependencies(Instantiation<?> instantiation, Set<Class<?>> traversedClasses) {
        List<DependencyDescription> dependencies = instantiation.getDependencies();
        Object[] values = new Object[dependencies.size()];
        for (int i = 0; i < dependencies.size(); ++i) {
            DependencyDescription dependency = dependencies.get(i);
            Object object = resolveByAnnotation(dependency);

            values[i] = (object == null)
                ? get(dependency.getType(), traversedClasses)
                : object;
        }
        return values;
    }

    @Nullable
    private Object resolveByAnnotation(DependencyDescription dependencyDescription) {
        Object o;
        for (DependencyHandler handler : config.getDependencyHandlers()) {
            try {
                if ((o = handler.resolveValue(this, dependencyDescription)) != null) {
                    return o;
                }
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }
        return null;
    }

    /**
     * Stores the given object with its class as key. Throws an exception if the key already has
     * a value associated to it.
     *
     * @param object the object to store
     */
    private void storeObject(Object object) {
        if (objects.containsKey(object.getClass())) {
            throw new IllegalStateException("There is already an object present for " + object.getClass());
        }
        InjectorUtils.checkNotNull(object, null); // should never happen
        objects.put(object.getClass(), object);
    }

    /**
     * Validates that none of the dependencies' types are present in the given collection
     * of traversed classes. This prevents circular dependencies.
     *
     * @param dependencies the dependencies of the class
     * @param traversedClasses the collection of traversed classes
     */
    private static void validateInjectionHasNoCircularDependencies(List<DependencyDescription> dependencies,
                                                                   Set<Class<?>> traversedClasses) {
        for (DependencyDescription dependency : dependencies) {
            Class<?> clazz = dependency.getType();
            if (traversedClasses.contains(clazz)) {
                throw new InjectorException("Found cyclic dependency - already traversed '" + clazz
                    + "' (full traversal list: " + traversedClasses + ")", clazz);
            }
        }
    }

}
