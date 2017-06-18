package ch.jalu.injector;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.context.ResolutionType;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;
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
        for (Handler handler : config.getHandlers()) {
            try {
                handler.onAnnotation(clazz, object);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
    }

    @Override
    public <T> T getSingleton(Class<T> clazz) {
        return resolve(SINGLETON, clazz);
    }

    @Override
    public <T> T newInstance(Class<T> clazz) {
        return resolve(REQUEST_SCOPED, clazz);
    }

    @Override
    public <T> T getIfAvailable(Class<T> clazz) {
        return clazz.cast(objects.get(clazz));
    }

    @Override
    public <T> T createIfHasDependencies(Class<T> clazz) {
        return resolve(REQUEST_SCOPED_IF_HAS_DEPENDENCIES, clazz);
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
        for (Handler handler : config.getHandlers()) {
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
        for (Handler handler : config.getHandlers()) {
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

    @SuppressWarnings("unchecked")
    private <T> T resolve(ResolutionType resolutionType, Class<?> clazz) {
        return (T) resolveObject(
            new ResolutionContext(this, new ObjectIdentifier(resolutionType, clazz)),
            new HashSet<>());
    }

    @Nullable
    private Object resolveObject(ResolutionContext context, Set<Class<?>> traversedClasses) {
        // TODO #49: Convert singleton store to a Handler impl.
        if (context.getIdentifier().getResolutionType() == StandardResolutionType.SINGLETON) {
            Object knownSingleton = objects.get(context.getIdentifier().getTypeAsClass());
            if (knownSingleton != null) {
                return knownSingleton;
            }
        }

        Resolution<?> resolution = findResolutionOrFail(context);

        traversedClasses.add(context.getIdentifier().getTypeAsClass());
        validateInjectionHasNoCircularDependencies(resolution, traversedClasses);

        for (ObjectIdentifier identifier : resolution.getDependencies()) {
            if (traversedClasses.contains(identifier.getTypeAsClass())) {
                throw new InjectorException("Found cyclic dependency - already traversed '"
                    + identifier.getTypeAsClass() + "' (full traversal list: " + traversedClasses + ")");
            }
        }

        List<ObjectIdentifier> dependencies = resolution.getDependencies();
        Object[] resolvedDependencies = dependencies.stream()
            .map(identifier -> new ResolutionContext(this, identifier))
            .map(dependencyContext -> resolveObject(dependencyContext, new HashSet<>(traversedClasses)))
            .toArray();
        Object object = resolution.instantiateWith(resolvedDependencies);

        if (resolution.isNewlyCreated()) {
            object = runPostConstructHandlers(object, context, resolution);
            if (context.getIdentifier().getResolutionType() == StandardResolutionType.SINGLETON) {
                register((Class) context.getOriginalIdentifier().getTypeAsClass(), object);
            }
        }
        return object;
    }

    private Resolution<?> findResolutionOrFail(ResolutionContext context) {
        try {
            for (Handler handler : config.getHandlers()) {
                Resolution<?> resolution = handler.resolve(context);
                if (resolution != null) {
                    return resolution;
                }
            }
        } catch (Exception e) {
            rethrowException(e);
        }

        final Class<?> clazz = context.getIdentifier().getTypeAsClass();
        if (!InjectorUtils.canInstantiate(clazz)) {
            String hint = clazz.isPrimitive()
                ? "Primitive types must be provided by default. "
                : clazz.isArray()
                    ? "By default, arrays cannot be injected. "
                    : "";
            throw new InjectorException(String.format("Did not find instantiation method for '%s'. "
                    + " %sThis class cannot be instantiated; please check the class or your handlers.",
                    clazz, hint));
        }
        throw new InjectorException("Did not find instantiation method for '" + context.getIdentifier().getTypeAsClass()
            + "'. Make sure your class conforms to one of the registered instantiations. If default: "
            + "make sure you have a constructor with @Inject or fields with @Inject. Fields with @Inject "
            + "require the default constructor");
    }

    private <T> T runPostConstructHandlers(T instance, ResolutionContext context, Resolution<?> resolution) {
        T object = instance;
        for (Handler handler : config.getHandlers()) {
            try {
                object = firstNotNull(handler.postProcess(object, context, resolution), object);
            } catch (Exception e) {
                rethrowException(e);
            }
        }
        return object;
    }

    /**
     * Validates that none of the dependencies' types are present in the given collection
     * of traversed classes. This prevents circular dependencies.
     *
     * @param resolution the resolution method to get the dependencies from
     * @param traversedClasses the collection of traversed classes
     */
    private static void validateInjectionHasNoCircularDependencies(Resolution<?> resolution,
                                                                   Set<Class<?>> traversedClasses) {
        for (ObjectIdentifier identifier : resolution.getDependencies()) {
            if (traversedClasses.contains(identifier.getTypeAsClass())) {
                throw new InjectorException("Found cyclic dependency - already traversed '"
                    + identifier.getTypeAsClass() + "' (full traversal list: " + traversedClasses + ")");
            }
        }
    }
}
