package ch.jalu.injector;

import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.context.UnresolvedInstantiationContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.annotationvalues.AnnotationValueHandler;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.provider.ProviderHandler;
import ch.jalu.injector.utils.InjectorUtils;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ch.jalu.injector.context.StandardResolutionType.REQUEST_SCOPED;
import static ch.jalu.injector.context.StandardResolutionType.REQUEST_SCOPED_IF_HAS_DEPENDENCIES;
import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;
import static ch.jalu.injector.utils.InjectorUtils.checkNotNull;
import static ch.jalu.injector.utils.InjectorUtils.firstNotNull;
import static ch.jalu.injector.utils.InjectorUtils.rethrowException;

/**
 * Implementation of {@link Injector}.
 */
public class InjectorImpl implements Injector {

    protected Map<Class<?>, Object> objects;
    protected InjectorConfig config;

    /**
     * Constructor.
     *
     * @param config injector configuration
     * @see InjectorBuilder
     */
    protected InjectorImpl(InjectorConfig config) {
        this.config = config;
        this.objects = new HashMap<>();
        this.objects.put(Injector.class, this);
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
        checkNotNull(object);
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
        return instantiate(
            new UnresolvedInstantiationContext<>(this, REQUEST_SCOPED, clazz),
            new HashSet<Class<?>>());
    }

    @Override
    public <T> T getIfAvailable(Class<T> clazz) {
        return clazz.cast(objects.get(clazz));
    }

    @Override
    public <T> T createIfHasDependencies(Class<T> clazz) {
        return instantiate(
            new UnresolvedInstantiationContext<>(this, REQUEST_SCOPED_IF_HAS_DEPENDENCIES, clazz),
            new HashSet<Class<?>>());
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
    public <T> void registerProvider(Class<T> clazz, Provider<? extends T> provider) {
        checkNotNull(clazz, "Class may not be null");
        checkNotNull(provider, "Provider may not be null");
        for (ProviderHandler handler : config.getProviderHandlers()) {
            try {
                handler.onProvider(clazz, provider);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
    }

    @Override
    public <T, P extends Provider<? extends T>> void registerProvider(Class<T> clazz, Class<P> providerClass) {
        checkNotNull(clazz, "Class may not be null");
        checkNotNull(providerClass, "Provider class may not be null");
        for (ProviderHandler handler : config.getProviderHandlers()) {
            try {
                handler.onProviderClass(clazz, providerClass);
            } catch (Exception e) {
                rethrowException(e);
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
     * @return instance or associated value (for annotations)
     */
    private <T> T get(Class<T> clazz, Set<Class<?>> traversedClasses) {
        if (objects.containsKey(clazz)) {
            return clazz.cast(objects.get(clazz));
        }

        UnresolvedInstantiationContext<T> context = new UnresolvedInstantiationContext<>(this, SINGLETON, clazz);
        // Add the clazz to the list of traversed classes in a new Set, so each path we take has its own Set.
        T object = instantiate(context, new HashSet<>(traversedClasses));
        register(clazz, object);
        return object;
    }

    /**
     * Instantiates the given class by locating its @Inject elements and retrieving
     * or instantiating the required instances.
     *
     * @param context the instantiation context
     * @param traversedClasses collection of classes already traversed
     * @param <T> the class' type
     * @return the instantiated object, or {@code null} if dependency lookup returned {@code null}
     */
    @Nullable
    private <T> T instantiate(UnresolvedInstantiationContext<T> context, Set<Class<?>> traversedClasses) {
        processPreConstructorHandlers(context);
        Instantiation<? extends T> instantiation = getInstantiation(context);
        traversedClasses.add(context.getMappedClass());
        validateInjectionHasNoCircularDependencies(instantiation.getDependencies(), traversedClasses);

        ResolvedInstantiationContext<T> resolvedContext = context.buildResolvedContext(instantiation);
        Object[] dependencies = resolveDependencies(resolvedContext, traversedClasses);
        if (dependencies == null) {
            return null;
        }
        T object = instantiation.instantiateWith(dependencies);
        return runPostConstructHandlers(object, resolvedContext);
    }

    /**
     * Resolves the dependencies for the given class instantiation, i.e. returns a collection that satisfy
     * the class' dependencies by retrieving elements or instantiating them where necessary.
     *
     * @param resolvedContext the initialization context
     * @param traversedClasses collection of traversed classes
     * @return array with the parameters to use in the constructor, {@code null} if a dependency is not available
     *         and {@link ch.jalu.injector.context.StandardResolutionType#REQUEST_SCOPED_IF_HAS_DEPENDENCIES} is the
     *         resolution type
     */
    @Nullable
    private Object[] resolveDependencies(ResolvedInstantiationContext<?> resolvedContext,
                                         Set<Class<?>> traversedClasses) {
        List<? extends DependencyDescription> dependencies = resolvedContext.getInstantiation().getDependencies();
        Object[] values = new Object[dependencies.size()];
        for (int i = 0; i < dependencies.size(); ++i) {
            DependencyDescription dependency = dependencies.get(i);
            Object object = resolveDependency(resolvedContext, dependency);
            if (object == null) {
                if (REQUEST_SCOPED_IF_HAS_DEPENDENCIES == resolvedContext.getResolutionType()
                    && objects.get(dependency.getTypeAsClass()) == null) {
                    return null;
                }
                object = get(dependency.getTypeAsClass(), traversedClasses);
            }
            values[i] = object;
        }
        return values;
    }

    private <T> Instantiation<? extends T> getInstantiation(UnresolvedInstantiationContext<T> context) {
        for (InstantiationProvider provider : config.getInstantiationProviders()) {
            Instantiation<? extends T> instantiation = provider.get(context);
            if (instantiation != null) {
                return instantiation;
            }
        }

        // No instantiation method was found, handle error with most appropriate message
        if (config.getInstantiationProviders().isEmpty()) {
            throw new InjectorException("You did not register any instantiation methods!");
        } else if (!InjectorUtils.canInstantiate(context.getMappedClass())) {
            throw new InjectorException("Did not find instantiation method for '" + context.getMappedClass()
                + "'. This class cannot be instantiated directly, please check the class or your handlers.");
        }
        throw new InjectorException("Did not find instantiation method for '" + context.getMappedClass()
            + "'. Make sure your class conforms to one of the registered instantiations. If default: "
            + "make sure you have a constructor with @Inject or fields with @Inject. Fields with @Inject "
            + "require the default constructor");
    }

    /**
     * Runs the given instantiation context through all registered {@link PreConstructHandler}s.
     *
     * @param unresolvedContext the instantiation context
     */
    private void processPreConstructorHandlers(UnresolvedInstantiationContext<?> unresolvedContext) {
        for (PreConstructHandler preConstructHandler : config.getPreConstructHandlers()) {
            try {
                preConstructHandler.accept(unresolvedContext);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
    }

    private <T> T runPostConstructHandlers(T instance, ResolvedInstantiationContext<T> resolvedContext) {
        T object = instance;
        for (PostConstructHandler postConstructHandler : config.getPostConstructHandlers()) {
            try {
                object = firstNotNull(postConstructHandler.process(object, resolvedContext), object);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
        return object;
    }

    @Nullable
    private Object resolveDependency(ResolvedInstantiationContext<?> resolvedContext,
                                     DependencyDescription dependencyDescription) {
        Object o;
        for (DependencyHandler handler : config.getDependencyHandlers()) {
            try {
                if ((o = handler.resolveValue(resolvedContext, dependencyDescription)) != null) {
                    return o;
                }
            } catch (Exception e) {
                rethrowException(e);
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
            Class<?> clazz = dependency.getTypeAsClass();
            if (traversedClasses.contains(clazz)) {
                throw new InjectorException("Found cyclic dependency - already traversed '" + clazz
                    + "' (full traversal list: " + traversedClasses + ")");
            }
        }
    }
}
