package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.instantiation.Instantiation;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
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

    private final String[] ALLOWED_PACKAGES;
    private final Map<Class<?>, Object> objects;

    /**
     * Constructor.
     *
     * @param allowedPackages list of allowed packages. Only classes whose package
     *        starts with any of the given entries will be instantiated
     */
    public InjectorImpl(String... allowedPackages) {
        ALLOWED_PACKAGES = allowedPackages;
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
            throw new InjectorException("There is already an object present for " + clazz);
        }
        InjectorUtils.checkNotNull(object);
        objects.put(clazz, object);
    }

    @Override
    public void provide(Class<? extends Annotation> annotation, Object value) {
        if (objects.containsKey(annotation)) {
            throw new InjectorException("Annotation @" + annotation.getClass().getSimpleName()
                + " already registered");
        }
        InjectorUtils.checkNotNull(value);
        objects.put(annotation, value);
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        return instantiate(clazz, new HashSet<Class<?>>());
    }

    @Override
    public <T> T getIfAvailable(Class<T> clazz) {
        if (Annotation.class.isAssignableFrom(clazz)) {
            throw new InjectorException("Annotations may not be retrieved in this way!");
        }
        return clazz.cast(objects.get(clazz));
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
        if (Annotation.class.isAssignableFrom(clazz)) {
            throw new InjectorException("Cannot retrieve annotated elements in this way!");
        } else if (objects.containsKey(clazz)) {
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
                + "the default constructor");
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
        Class<?>[] annotations = instantiation.getDependencyAnnotations();
        Object[] values = new Object[dependencies.length];
        for (int i = 0; i < dependencies.length; ++i) {
            if (annotations[i] == null) {
                values[i] = get(dependencies[i], traversedClasses);
            } else {
                Object value = objects.get(annotations[i]);
                if (value == null) {
                    throw new InjectorException("Value for field with @" + annotations[i].getSimpleName()
                        + " must be registered beforehand");
                }
                values[i] = value;
            }
        }
        return values;
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
        InjectorUtils.checkNotNull(object);
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
                    + "' (full traversal list: " + traversedClasses + ")");
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
            throw new InjectorException("Primitive types must be provided explicitly (or use an annotation).");
        }
        String packageName = clazz.getPackage().getName();
        for (String allowedPackage : ALLOWED_PACKAGES) {
            if (packageName.startsWith(allowedPackage)) {
                return;
            }
        }
        throw new InjectorException("Class " + clazz + " with package " + packageName + " is outside of the "
            + "allowed packages. It must be provided explicitly or the package must be passed to the constructor.");
    }

    /**
     * Executes an object's method annotated with {@link PostConstruct} if present.
     * Throws an exception if there are multiple such methods, or if the method is static.
     *
     * @param object the object to execute the post construct method for
     */
    private static void executePostConstructMethod(Object object) {
        Method postConstructMethod = InjectionHelper.getAndValidatePostConstructMethod(object.getClass());
        ReflectionUtils.invokeMethod(postConstructMethod, object);
    }

    private static void validateInstantiable(Class<?> clazz) {
        if (clazz.isEnum() || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            throw new InjectorException("Class " + clazz.getSimpleName() + " cannot be instantiated");
        }
    }

}
