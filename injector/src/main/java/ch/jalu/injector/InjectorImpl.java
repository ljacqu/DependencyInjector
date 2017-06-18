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
import java.util.List;
import java.util.Map;

import static ch.jalu.injector.context.StandardResolutionType.REQUEST_SCOPED;
import static ch.jalu.injector.context.StandardResolutionType.REQUEST_SCOPED_IF_HAS_DEPENDENCIES;
import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;
import static ch.jalu.injector.utils.InjectorUtils.checkNotNull;
import static ch.jalu.injector.utils.InjectorUtils.containsNullValue;
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
        try {
            for (Handler handler : config.getHandlers()) {
                handler.onAnnotation(clazz, object);
            }
        } catch (Exception e) {
            rethrowException(e);
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
        try {
            for (Handler handler : config.getHandlers()) {
                handler.onProvider(clazz, provider);
            }
        } catch (Exception e) {
            rethrowException(e);
        }
    }

    @Override
    public <T, P extends Provider<? extends T>> void registerProvider(Class<T> clazz, Class<P> providerClass) {
        checkNotNull(clazz, "Class may not be null");
        checkNotNull(providerClass, "Provider class may not be null");
        try {
            for (Handler handler : config.getHandlers()) {
                handler.onProviderClass(clazz, providerClass);
            }
        } catch (Exception e) {
            rethrowException(e);
        }
    }

    public InjectorConfig getConfig() {
        return config;
    }

    @SuppressWarnings("unchecked")
    private <T> T resolve(ResolutionType resolutionType, Class<?> clazz) {
        return (T) resolveContext(
            new ResolutionContext(this, new ObjectIdentifier(resolutionType, clazz)));
    }

    /**
     * Returns the object as defined by the given context.
     *
     * @param context the context to resolve the object for
     * @return the resolved object, {@code null} if the context specifies it is optional and some criteria is not met
     */
    @Nullable
    protected Object resolveContext(ResolutionContext context) {
        // TODO #49: Convert singleton store to a Handler impl.
        if (context.getIdentifier().getResolutionType() == StandardResolutionType.SINGLETON) {
            Object knownSingleton = objects.get(context.getIdentifier().getTypeAsClass());
            if (knownSingleton != null) {
                return knownSingleton;
            }
        }

        Resolution<?> resolution = findResolutionOrFail(context);
        if (isContextChildOfOptionalRequest(context) && resolution.isInstantiation()) {
            return null;
        }

        Object[] resolvedDependencies = resolveDependencies(context, resolution);
        if (containsNullValue(resolvedDependencies)) {
            throwForUnexpectedNullDependency(context);
            return null;
        }

        Object object = runPostConstructHandlers(resolution.instantiateWith(resolvedDependencies), context, resolution);
        if (resolution.isInstantiation() && context.getIdentifier().getResolutionType() == SINGLETON) {
            register((Class) context.getOriginalIdentifier().getTypeAsClass(), object);
        }
        return object;
    }

    /**
     * Resolves the dependencies as defined by the given resolution.
     * If a dependency is resolved to {@code null}, the process is aborted and the remaining dependencies
     * are not resolved.
     *
     * @param context the resolution context
     * @param resolution the resolution whose dependencies should be provided
     * @return array with the dependencies, in the same order as given by the resolution
     */
    protected Object[] resolveDependencies(ResolutionContext context, Resolution<?> resolution) {
        final int totalDependencies = resolution.getDependencies().size();
        final Object[] resolvedDependencies = new Object[totalDependencies];

        int index = 0;
        for (ObjectIdentifier dependencyId : resolution.getDependencies()) {
            Object dependency = resolveContext(context.createChildContext(dependencyId));
            if (dependency == null) {
                break;
            }
            resolvedDependencies[index] = dependency;
            ++index;
        }
        return resolvedDependencies;
    }

    /**
     * Called when a resolved dependency is null, this method may throw an exception in the cases when this
     * should not happen. If this method does not throw an exception, null is returned from {@link #resolveContext}.
     *
     * @param context the resolution context
     */
    protected void throwForUnexpectedNullDependency(ResolutionContext context) {
        if (context.getIdentifier().getResolutionType() == REQUEST_SCOPED_IF_HAS_DEPENDENCIES
            || isContextChildOfOptionalRequest(context)) {
            // Situation where null may occur, so throw no exception
            return;
        }
        throw new InjectorException("Found null returned as dependency while resolving '"
            + context.getIdentifier() + "'");
    }

    private static boolean isContextChildOfOptionalRequest(ResolutionContext context) {
        return !context.getParents().isEmpty()
            && context.getParents().get(0).getIdentifier().getResolutionType() == REQUEST_SCOPED_IF_HAS_DEPENDENCIES;
    }

    /**
     * Calls the defined handlers and returns the first {@link Resolution} that is returned based on
     * the provided resolution context. Throws an exception if no handler returned a resolution.
     *
     * @param context the context to find the resolution for
     * @return the resolution
     */
    protected Resolution<?> findResolutionOrFail(ResolutionContext context) {
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
            throw new InjectorException(String.format("Did not find instantiation method for '%s'."
                    + " %sThis class cannot be instantiated; please check the class or your handlers.",
                    clazz, hint));
        }
        throw new InjectorException("Did not find instantiation method for '" + context.getIdentifier().getTypeAsClass()
            + "'. Make sure your class conforms to one of the registered instantiations. If default: "
            + "make sure you have a constructor with @Inject or fields with @Inject. Fields with @Inject "
            + "require the default constructor");
    }

    /**
     * Invokes the handler's post construct method when appropriate. Returns the object as returned by the
     * handlers, which may be different from the provided one.
     *
     * @param instance the object that was resolved
     * @param context the resolution context
     * @param resolution the resolution used to get the object
     * @param <T> the object's type
     * @return the object to use (as post construct methods may change it)
     */
    protected <T> T runPostConstructHandlers(T instance, ResolutionContext context, Resolution<?> resolution) {
        if (!resolution.isInstantiation()) {
            return instance;
        }

        T object = instance;
        try {
            for (Handler handler : config.getHandlers()) {
                object = firstNotNull(handler.postProcess(object, context, resolution), object);
            }
        } catch (Exception e) {
            rethrowException(e);
        }
        return object;
    }
}
