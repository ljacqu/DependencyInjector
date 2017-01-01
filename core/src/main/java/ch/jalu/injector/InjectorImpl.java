package ch.jalu.injector;

import ch.jalu.injector.annotations.Provides;
import ch.jalu.injector.config.InjectorConfiguration;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.annotationvalues.AnnotationValueHandler;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.provider.ProviderHandler;
import ch.jalu.injector.handlers.provider.UninitializedProviderByClass;
import ch.jalu.injector.handlers.provider.UninitializedProviderByMethod;
import ch.jalu.injector.utils.InjectorUtils;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.jalu.injector.utils.InjectorUtils.checkNotNull;
import static ch.jalu.injector.utils.InjectorUtils.firstNotNull;
import static ch.jalu.injector.utils.InjectorUtils.rethrowException;

/**
 * Implementation of {@link Injector}.
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
            throw new InjectorException("There is already an object present for " + clazz);
        }
        InjectorUtils.checkNotNull(object);
        objects.put(clazz, object);
    }

    @Override
    public void provide(Class<? extends Annotation> clazz, Object object) {
        checkNotNull(clazz, "Provided annotation may not be null");
        for (AnnotationValueHandler annotationValueHandler : config.getAnnotationValueHandlers()) {
            try {
                annotationValueHandler.processProvided(clazz, object);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        Class<? extends T> mappedClass = processPreConstructorHandlers(clazz);
        return instantiate(mappedClass, new HashSet<Class<?>>());
    }

    @Override
    public <T> T getIfAvailable(Class<T> clazz) {
        return clazz.cast(objects.get(clazz));
    }

    @Override
    public <T> T createIfHasDependencies(Class<T> clazz) {
        Class<? extends T> mappedClass = processPreConstructorHandlers(clazz);
        Instantiation<? extends T> instantiation = getInstantiation(mappedClass);
        List<Object> dependencies = new ArrayList<>();
        for (DependencyDescription description : instantiation.getDependencies()) {
            Object object = objects.get(description.getType());
            if (object == null) {
                return null;
            }
            dependencies.add(object);
        }
        return runPostConstructHandlers(instantiation.instantiateWith(dependencies.toArray()));
    }

    @Override
    public <T> Collection<T> retrieveAllOfType(Class<T> clazz) {
        List<T> instances = new ArrayList<>();
        for (Object object : objects.values()) {
            if (clazz.isInstance(object)) {
                instances.add(clazz.cast(object));
            }
        }
        return instances;
    }

    @Override
    public void addConfiguration(InjectorConfiguration configuration) {
        for (Method method : configuration.getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(Provides.class)) {
                Class providerType = method.getReturnType();
                if (Void.TYPE.equals(providerType)) {
                    throw new InjectorException("@Provides method '" + method + "' has void return type");
                }
                registerProviderInstantiation(providerType, new UninitializedProviderByMethod<>(method, configuration));
            }
        }
    }

    @Override
    public <T> void registerProvider(Class<T> clazz, Provider<? extends T> provider) {
        checkNotNull(clazz, "Class may not be null");
        checkNotNull(provider, "Provider may not be null");
        for (ProviderHandler handler : config.getProviderHandlers()) {
            try {
                handler.onProvider(clazz, provider);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }
    }

    @Override
    public <T> void registerProvider(Class<T> clazz, Class<? extends Provider<? extends T>> providerClass) {
        checkNotNull(clazz, "Class may not be null");
        checkNotNull(providerClass, "Provider class may not be null");
        registerProviderInstantiation(clazz, new UninitializedProviderByClass<>(providerClass));
    }

    private <T> void registerProviderInstantiation(Class<T> clazz,
                                                 Instantiation<? extends Provider<? extends T>> providerInstantiation) {
        for (ProviderHandler handler : config.getProviderHandlers()) {
            try {
                handler.onProvider(clazz, providerInstantiation);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }
    }

    public InjectorConfig getConfig() {
        return config;
    }

    /**
     * Returns an instance of the given class by retrieving it or by instantiating it if not yet present.
     *
     * @param clazz the class to retrieve the singleton instance for
     * @param traversedClasses the list of traversed classes
     * @param <T> the class' type
     * @param <U> the remapped type (PreConstructHandler may rewrite the actual type of the object)
     * @return instance or associated value (for annotations)
     */
    private <T, U extends T> T get(Class<T> clazz, Set<Class<?>> traversedClasses) {
        if (objects.containsKey(clazz)) {
            return clazz.cast(objects.get(clazz));
        }

        // First time we come across clazz, need to instantiate it. Validate that we can do so
        Class<U> mappedClass = processPreConstructorHandlers(clazz);

        // Add the clazz to the list of traversed classes in a new Set, so each path we take has its own Set.
        traversedClasses = new HashSet<>(traversedClasses);
        traversedClasses.add(mappedClass);
        U object = instantiate(mappedClass, traversedClasses);
        register(clazz, object);
        if (mappedClass != clazz) {
            register(mappedClass, object);
        }
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
        Instantiation<T> instantiation = getInstantiation(clazz);
        validateInjectionHasNoCircularDependencies(instantiation.getDependencies(), traversedClasses);

        Object[] dependencies = resolveDependencies(instantiation, traversedClasses);
        T object = instantiation.instantiateWith(dependencies);
        return runPostConstructHandlers(object);
    }

    private <T> Instantiation<T> getInstantiation(Class<T> clazz) {
        for (InstantiationProvider provider : config.getInstantiationProviders()) {
            Instantiation<T> instantiation = provider.get(clazz);
            if (instantiation != null) {
                return instantiation;
            }
        }

        // No instantiation method was found, handle error with most appropriate message
        if (config.getInstantiationProviders().isEmpty()) {
            throw new InjectorException("You did not register any instantiation methods!");
        } else if (!InjectorUtils.canInstantiate(clazz)) {
            throw new InjectorException("Did not find instantiation method for '" + clazz + "'. This class cannot "
                + "be instantiated directly, please check the class or your handlers.");
        }
        throw new InjectorException("Did not find instantiation method for '" + clazz + "'. Make sure your class "
            + "conforms to one of the registered instantiations. If default: make sure you have "
            + "a constructor with @Inject or fields with @Inject. Fields with @Inject require "
            + "the default constructor");
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
        List<? extends DependencyDescription> dependencies = instantiation.getDependencies();
        Object[] values = new Object[dependencies.size()];
        for (int i = 0; i < dependencies.size(); ++i) {
            values[i] = resolveDependency(dependencies.get(i), traversedClasses);
        }
        return values;
    }

    /**
     * Runs all registered {@link PreConstructHandler}s for the given class and returns the result.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @param <U> type of the remapped class (child of T or T)
     * @return the potentially changed class
     */
    @SuppressWarnings("unchecked")
    private <T, U extends T> Class<U> processPreConstructorHandlers(Class<T> clazz) {
        Class mappedClass = clazz;
        for (PreConstructHandler preConstructHandler : config.getPreConstructHandlers()) {
            try {
                mappedClass = firstNotNull(preConstructHandler.accept(mappedClass), mappedClass);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }
        return mappedClass;
    }

    private <T> T runPostConstructHandlers(T instance) {
        T object = instance;
        for (PostConstructHandler postConstructHandler : config.getPostConstructHandlers()) {
            try {
                object = firstNotNull(postConstructHandler.process(object), object);
            } catch (Exception e) {
                InjectorUtils.rethrowException(e);
            }
        }
        return object;
    }

    @Override
    public Object resolveDependency(DependencyDescription dependency) {
        return resolveDependency(dependency, new HashSet<Class<?>>());
    }

    private Object resolveDependency(DependencyDescription dependency, Set<Class<?>> traversedClasses) {
        Object object = getDependencyFromHandlers(dependency);

        return (object == null)
                ? get(dependency.getType(), traversedClasses)
                : object;
    }

    private Object getDependencyFromHandlers(DependencyDescription dependencyDescription) {
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
     * Validates that none of the dependencies' types are present in the given collection
     * of traversed classes. This prevents circular dependencies.
     *
     * @param dependencies the dependencies of the class
     * @param traversedClasses the collection of traversed classes
     */
    private static void validateInjectionHasNoCircularDependencies(List<? extends DependencyDescription> dependencies,
                                                                   Set<Class<?>> traversedClasses) {
        for (DependencyDescription dependency : dependencies) {
            Class<?> clazz = dependency.getType();
            if (traversedClasses.contains(clazz)) {
                throw new InjectorException("Found cyclic dependency - already traversed '" + clazz
                    + "' (full traversal list: " + traversedClasses + ")");
            }
        }
    }

}
