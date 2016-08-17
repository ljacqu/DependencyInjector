package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.jalu.injector.utils.InjectorUtils.checkArgument;

/**
 * Default handler for {@link Provider} objects. Registers providers and classes and creates
 * {@link Instantiation} objects for classes it can handle.
 */
public class ProviderHandlerImpl implements ProviderHandler, InstantiationProvider, DependencyHandler {

    protected Map<Class<?>, ProviderWrappedInstantiation<?>> providers = new HashMap<>();

    @Override
    public <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) {
        checkArgument(!providers.containsKey(clazz), "Provider already registered for " + clazz);
        providers.put(clazz, new ProviderInstantiation<>(provider));
    }

    @Override
    public <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz, Class<P> providerClass) {
        checkArgument(!providers.containsKey(clazz), "Provider already registered for " + clazz);
        providers.put(clazz, new UninitializedProviderInstantiation<>(clazz, providerClass));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Instantiation<T> get(Class<T> clazz) {
        return (Instantiation<T>) providers.get(clazz);
    }

    @Override
    public Object resolveValue(Injector injector, DependencyDescription dependencyDescription) {
        if (dependencyDescription.getType() == Provider.class) {
            Class<?> genericType = ReflectionUtils.getGenericType(dependencyDescription.getGenericType());
            if (genericType == null) {
                throw new InjectorException("Injection of a provider was requested but no generic type was given");
            }

            ProviderWrappedInstantiation<?> instantiation = providers.get(genericType);
            if (instantiation != null) {
                return instantiation.getProvider(injector);
            }
            return constructStandardProvider(genericType, injector);
        }
        return null;
    }

    private static <T> Provider<T> constructStandardProvider(final Class<T> genericType, final Injector injector) {
        return new Provider<T>() {
            @Override
            public T get() {
                return injector.newInstance(genericType);
            }
        };
    }

    private <T> void saveConstructedProvider(Class<T> clazz, Provider<? extends T> provider) {
        providers.put(clazz, new ProviderInstantiation<>(provider));
    }

    private interface ProviderWrappedInstantiation<T> extends Instantiation<T> {

        Provider<? extends T> getProvider(Injector injector);

    }

    /**
     * Simple instantiation that creates an object with the known provider.
     *
     * @param <T> the type of the class to create
     */
    private static final class ProviderInstantiation<T> implements ProviderWrappedInstantiation<T> {

        private final Provider<? extends T> provider;

        ProviderInstantiation(Provider<? extends T> provider) {
            this.provider = provider;
        }

        @Override
        public List<DependencyDescription> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        public T instantiateWith(Object... values) {
            return provider.get();
        }

        @Override
        public Provider<? extends T> getProvider(Injector injector) {
            return provider;
        }
    }

    /**
     * Instantiation that internally creates the required provider first. This is triggered by
     * declaring the provider class as a dependency, making the injector create the provider
     * class first.
     *
     * @param <T> the type of the class to create
     */
    private final class UninitializedProviderInstantiation<T> implements ProviderWrappedInstantiation<T> {

        private final Class<T> clazz;
        private final Class<? extends Provider<? extends T>> providerClass;

        UninitializedProviderInstantiation(Class<T> clazz, Class<? extends Provider<? extends T>> providerClass) {
            this.providerClass = providerClass;
            this.clazz = clazz;
        }

        @Override
        public List<DependencyDescription> getDependencies() {
            return Collections.singletonList(
                new DependencyDescription(providerClass, clazz));
        }

        @Override
        public T instantiateWith(Object... values) {
            if (values.length == 1 && values[0] instanceof Provider<?>) {
                @SuppressWarnings("unchecked")
                Provider<? extends T> provider = (Provider<? extends T>) values[0];
                T object = provider.get();
                // The injector passed us the provider, so save it in the map for future uses
                saveConstructedProvider(clazz, provider);
                return object;
            }
            throw new InjectorException("Provider is required as argument");
        }

        @Override
        public Provider<? extends T> getProvider(Injector injector) {
            Provider<? extends T> provider = injector.getSingleton(providerClass);
            saveConstructedProvider(clazz, provider);
            return provider;
        }
    }
}
