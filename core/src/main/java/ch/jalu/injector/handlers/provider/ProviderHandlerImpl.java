package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.provider.WrappedUninitializedProvider.Consumer;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Provider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.jalu.injector.utils.InjectorUtils.checkArgument;
import static ch.jalu.injector.utils.InjectorUtils.checkNotNull;

/**
 * Default handler for {@link Provider} objects. Registers providers and classes and creates
 * {@link Instantiation} objects for classes it can handle.
 */
public class ProviderHandlerImpl implements ProviderHandler, InstantiationProvider, DependencyHandler {

    protected Map<Class<?>, Object> providers = new HashMap<>();

    @Override
    public <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) {
        addProvider(clazz, new ProviderByObject<>(provider));
    }

    @Override
    public <T> void onProvider(Class<T> clazz, Instantiation<? extends Provider<? extends T>> providerInstantiation) {
        addProvider(clazz, providerInstantiation);
    }

    private void addProvider(Class<?> clazz, Object object) {
        checkNotNull(clazz);
        checkNotNull(object);
        checkArgument(!providers.containsKey(clazz), "Provider already registered for " + clazz);
        providers.put(clazz, object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Instantiation<T> get(final Class<T> clazz) {
        Object provider = providers.get(clazz);
        if (provider instanceof ProviderByObject) {
            return (ProviderByObject<T>) provider;
        } else if (provider instanceof Instantiation<?>) {
            Instantiation<? extends Provider<? extends T>> providerInstantiation = (Instantiation) provider;
            WrappedUninitializedProvider<T> wrappedInstantiation =
                    new WrappedUninitializedProvider<>(providerInstantiation);
            wrappedInstantiation.setProviderConsumer(new Consumer<Provider<? extends T>>() {
                @Override
                public void accept(Provider<? extends T> provider) {
                    providers.put(clazz, new ProviderByObject<>(provider));
                }
            });
            return wrappedInstantiation;
        }
        return (Instantiation<T>) providers.get(clazz);
    }

    @Override
    public Object resolveValue(Injector injector, DependencyDescription dependencyDescription) {
        if (dependencyDescription.getType() == Provider.class) {
            Class<?> genericType = ReflectionUtils.getGenericType(dependencyDescription.getGenericType());
            if (genericType == null) {
                throw new InjectorException("Injection of a provider was requested but no generic type was given");
            }

            Object provider = providers.get(genericType);
            if (provider instanceof ProviderByObject<?>) {
                return ((ProviderByObject<?>) provider).getProvider();
            } else if (provider instanceof Instantiation<?>) {
                ProviderByObject<?> constructedProvider =
                    createProviderInstantiation(injector, (Instantiation) provider);
                providers.put(genericType, constructedProvider);
                return constructedProvider.getProvider();
            } else if (provider != null) {
                // This should never happen
                throw new IllegalStateException("Unexpected entry '" + provider + "' of type '" + provider.getClass()
                    + "' for generic type '" + genericType + "'");
            }
            return constructStandardProvider(genericType, injector);
        }
        return null;
    }

    private <T> ProviderByObject<T> createProviderInstantiation(Injector injector,
                                                                Instantiation<Provider<T>> uninitializedProvider) {
        List<DependencyDescription> dependencies = uninitializedProvider.getDependencies();
        Object[] values = new Object[dependencies.size()];
        for (int i = 0; i < dependencies.size(); ++i) {
            values[i] = injector.resolveDependency(dependencies.get(i));
        }

        Provider<T> provider = uninitializedProvider.instantiateWith(values);
        return new ProviderByObject<>(provider);
    }

    private static <T> Provider<T> constructStandardProvider(final Class<T> genericType, final Injector injector) {
        return new Provider<T>() {
            @Override
            public T get() {
                return injector.newInstance(genericType);
            }
        };
    }

}
