package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.Nullable;
import javax.inject.Provider;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;
import static ch.jalu.injector.utils.InjectorUtils.checkArgument;

/**
 * Default handler for {@link Provider} objects. Registers providers and classes and creates
 * {@link Resolution} objects for classes it can handle.
 */
public class ProviderHandler implements Handler {

    protected Map<Class<?>, ProviderBasedInstantiation<?>> providers = new HashMap<>();

    @Override
    public <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) {
        checkArgument(!providers.containsKey(clazz), "Provider already registered for " + clazz);
        providers.put(clazz, new InstantiationByProvider<>(provider));
    }

    @Override
    public <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz, Class<P> providerClass) {
        checkArgument(!providers.containsKey(clazz), "Provider already registered for " + clazz);
        providers.put(clazz, new InstantiationByProviderClass<>(providerClass));
    }

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        if (Provider.class.equals(context.getIdentifier().getTypeAsClass())) {
            return handleProviderRequest(context);
        }
        return providers.get(context.getIdentifier().getTypeAsClass());
    }

    @Nullable
    private Resolution<?> handleProviderRequest(ResolutionContext context) {
        Class<?> genericType = ReflectionUtils.getGenericType(context.getIdentifier().getType());
        if (genericType == null) {
            throw new InjectorException("Injection of a provider was requested but no generic type was given");
        }
        ProviderBasedInstantiation<?> givenInstantiation = providers.get(genericType);
        if (givenInstantiation == null) {
            Provider<?> defaultProvider = () -> context.getInjector().newInstance(genericType);
            return new SimpleResolution<>(defaultProvider);
        }
        return givenInstantiation.createProviderResolution();
    }

    /**
     * Resolution that instantiates an object with a predefined Provider.
     *
     * @param <T> the object type
     */
    private interface ProviderBasedInstantiation<T> extends Resolution<T> {

        Resolution<Provider<? extends T>> createProviderResolution();

    }

    /**
     * Resolution of an object via a known provider.
     *
     * @param <T> the object type
     */
    private static final class InstantiationByProvider<T> implements ProviderBasedInstantiation<T> {
        private final Provider<? extends T> provider;

        InstantiationByProvider(Provider<? extends T> provider) {
            this.provider = provider;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return Collections.emptyList();
        }

        @Override
        public T instantiateWith(Object... values) {
            InjectorUtils.checkArgument(values.length == 0, "No dependencies expected");
            return provider.get();
        }

        @Override
        public boolean isInstantiation() {
            return true;
        }

        @Override
        public Resolution<Provider<? extends T>> createProviderResolution() {
            return new SimpleResolution<>(provider);
        }
    }

    /**
     * Resolution of an object with a known provider class.
     *
     * @param <T> the object type
     */
    private static final class InstantiationByProviderClass<T> implements ProviderBasedInstantiation<T> {
        private final Class<? extends Provider<? extends T>> providerClass;

        InstantiationByProviderClass(Class<? extends Provider<? extends T>> providerClass) {
            this.providerClass = providerClass;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return Collections.singletonList(new ObjectIdentifier(SINGLETON, providerClass));
        }

        @Override
        @SuppressWarnings("unchecked")
        public T instantiateWith(Object... values) {
            InjectorUtils.checkArgument(values.length == 1 && providerClass.isInstance(values[0]),
                "Expected one dependency of type " + providerClass);
            return ((Provider<? extends T>) values[0]).get();
        }

        @Override
        public boolean isInstantiation() {
            return true;
        }

        @Override
        public Resolution<Provider<? extends T>> createProviderResolution() {
            // Workaround: return an Instantiation object that takes the actual class as dependency and simply returns
            // it as the result. This way whatever concrete class is mapped to function as Provider<T> is created as if
            // we did injector.getSingleton(providerClass) and it gets registered as such.

            return new Resolution<Provider<? extends T>>() {
                @Override
                public List<ObjectIdentifier> getDependencies() {
                    return Collections.singletonList(new ObjectIdentifier(SINGLETON, providerClass));
                }

                @Override
                @SuppressWarnings("unchecked")
                public Provider<? extends T> instantiateWith(Object... values) {
                    InjectorUtils.checkArgument(values.length == 1 && providerClass.isInstance(values[0]),
                        "Expected one dependency of type " + providerClass);
                    return (Provider<? extends T>) values[0];
                }
            };
        }
    }
}
