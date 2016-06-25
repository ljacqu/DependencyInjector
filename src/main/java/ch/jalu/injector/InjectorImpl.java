package ch.jalu.injector;

import ch.jalu.injector.handlers.annotations.AnnotationHandler;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.instantiation.Instantiation;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        config.injectAnnotationHandlerFields(this);
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
        validatePackage(clazz);
        validateInstantiable(clazz);

        // Add the clazz to the list of traversed classes in a new Set, so each path we take has its own Set.
        traversedClasses = new HashSet<>(traversedClasses);
        traversedClasses.add(clazz);
        T object = instantiate(clazz, traversedClasses);
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
        executePostConstructMethod(object);
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
        Class<?>[] dependencies = instantiation.getDependencies();
        Annotation[][] annotations = instantiation.getDependencyAnnotations();
        Object[] values = new Object[dependencies.length];
        for (int i = 0; i < dependencies.length; ++i) {
            Object object = resolveAnnotation(dependencies[i], annotations[i]);
            if (object == null) {
                values[i] = get(dependencies[i], traversedClasses);
            } else {
                values[i] = object;
            }
        }
        return values;
    }

    @Nullable
    private Object resolveAnnotation(Class<?> type, Annotation... annotations) {
        Object o;
        for (AnnotationHandler handler : config.getAnnotationHandlers()) {
            try {
                if ((o = handler.resolveValue(type, annotations)) != null) {
                    return o;
                }
            } catch (Exception e) {
                if (e instanceof InjectorException) {
                    throw (InjectorException) e;
                }
                throw new InjectorException(
                    "Error while processing '" + type + "' in '" + handler.getClass() + "'", e, handler.getClass());
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
    private static void validateInjectionHasNoCircularDependencies(Class<?>[] dependencies,
                                                                   Set<Class<?>> traversedClasses) {
        for (Class<?> clazz : dependencies) {
            if (traversedClasses.contains(clazz)) {
                throw new InjectorException("Found cyclic dependency - already traversed '" + clazz
                    + "' (full traversal list: " + traversedClasses + ")", clazz);
            }
        }
    }

    /**
     * Validates the package of a parameter type to ensure that it is part of the allowed packages.
     * This ensures that we don't try to instantiate things that are beyond our reach in case some
     * external parameter type has not been registered.
     *
     * @param clazz the class to validate
     */
    private void validatePackage(Class<?> clazz) {
        if (clazz.getPackage() == null) {
            throw new InjectorException("Primitive types must be provided explicitly (or use an annotation).", clazz);
        }
        String packageName = clazz.getPackage().getName();
        if (!config.isAllowedPackage(packageName)) {
            throw new InjectorException("Class " + clazz + " with package " + packageName + " is outside of the "
                + "allowed packages. It must be provided explicitly or the package must be passed to the constructor.",
                    clazz);
        }
    }

    /**
     * Executes an object's method annotated with {@link PostConstruct} if present.
     * Throws an exception if there are multiple such methods, or if the method is static.
     *
     * @param object the object to execute the post construct method for
     */
    private static void executePostConstructMethod(Object object) {
        Method postConstructMethod = InjectionHelper.getAndValidatePostConstructMethod(object.getClass());
        if (postConstructMethod != null) {
            ReflectionUtils.invokeMethod(postConstructMethod, object);
        }
    }

    private static void validateInstantiable(Class<?> clazz) {
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new InjectorException("Class " + clazz.getSimpleName() + " cannot be instantiated", clazz);
        }
    }

}
