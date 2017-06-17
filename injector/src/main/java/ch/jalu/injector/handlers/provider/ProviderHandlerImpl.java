package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.SimpleObjectResolution;
import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.Nullable;
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
public class ProviderHandlerImpl implements Handler {

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
    public Instantiation<?> get(UnresolvedContext context) {
        if (Provider.class.equals(context.getIdentifier().getTypeAsClass())) {
            return handleProviderRequest(context);
        }
        return providers.get(context.getIdentifier().getTypeAsClass());
    }

    @Nullable
    private Instantiation<?> handleProviderRequest(UnresolvedContext context) {
        Class<?> genericType = ReflectionUtils.getGenericType(context.getIdentifier().getType());
        if (genericType == null) {
            throw new InjectorException("Injection of a provider was requested but no generic type was given");
        }
        ProviderBasedInstantiation<?> givenInstantiation = providers.get(genericType);
        if (givenInstantiation == null) {
            Provider<?> defaultProvider = () -> context.getInjector().newInstance(genericType);
            return new SimpleObjectResolution<>(defaultProvider);
        }
        return givenInstantiation.createProviderInstantiation();
    }

    private interface ProviderBasedInstantiation<T> extends Instantiation<T> {

        Instantiation<Provider<? extends T>> createProviderInstantiation();

    }

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
        public boolean saveIfSingleton() {
            return true;
        }

        @Override
        public Instantiation<Provider<? extends T>> createProviderInstantiation() {
            return new SimpleObjectResolution<>(provider);
        }
    }

    private static final class InstantiationByProviderClass<T> implements ProviderBasedInstantiation<T> {
        private final Class<? extends Provider<? extends T>> providerClass;

        InstantiationByProviderClass(Class<? extends Provider<? extends T>> providerClass) {
            this.providerClass = providerClass;
        }

        @Override
        public List<ObjectIdentifier> getDependencies() {
            return Collections.singletonList(new ObjectIdentifier(providerClass));
        }

        @Override
        public T instantiateWith(Object... values) {
            InjectorUtils.checkArgument(values.length == 1 && providerClass.isInstance(values[0]),
                "Expected one dependency of type " + providerClass);
            return ((Provider<? extends T>) values[0]).get();
        }

        @Override
        public boolean saveIfSingleton() {
            return true;
        }

        @Override
        public Instantiation<Provider<? extends T>> createProviderInstantiation() {
            // Workaround: return an Instantiation object that takes the actual class as dependency and simply returns
            // it as the result. This way whatever concrete class is mapped to function as Provider<T> is created as if
            // we did injector.getSingleton(providerClass) and it gets registered as such.

            return new Instantiation<Provider<? extends T>>() {
                @Override
                public List<ObjectIdentifier> getDependencies() {
                    return Collections.singletonList(new ObjectIdentifier(providerClass));
                }

                @Override
                public Provider<? extends T> instantiateWith(Object... values) {
                    InjectorUtils.checkArgument(values.length == 1 && providerClass.isInstance(values[0]),
                        "Expected one dependency of type " + providerClass);
                    return (Provider<? extends T>) values[0];
                }
            };
        }
    }
}
