package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.inject.Provider;
import java.util.List;

/**
 * Provides an instantiation object for a given type that is associated with a provider
 * which has not been initialized yet. In other words, it provides a simple Instantiation
 * interface for the given type on the outside and creates the missing provider behind
 * the scenes (e.g. provider class that has not been constructed yet).
 */
public class WrappedUninitializedProvider<T> implements Instantiation<T> {

    private final Instantiation<? extends Provider<? extends T>> providerInstantiation;
    private Consumer<Provider<? extends T>> consumer;

    /**
     * Constructor.
     *
     * @param providerInstantiation the instantiation method of the provider to use
     */
    public WrappedUninitializedProvider(Instantiation<? extends Provider<? extends T>> providerInstantiation) {
        this.providerInstantiation = providerInstantiation;
    }

    /**
     * Sets a consumer taking the newly created provider, typically to register it
     * for future re-use. See current usages.
     *
     * @param consumer consumer to call with the constructed provider
     */
    public void setProviderConsumer(Consumer<Provider<? extends T>> consumer) {
        this.consumer = consumer;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        return providerInstantiation.getDependencies();
    }

    @Override
    public T instantiateWith(Object... values) {
        Provider<? extends T> provider = providerInstantiation.instantiateWith(values);
        if (consumer != null) {
            consumer.accept(provider);
        }
        return provider.get();
    }

    /**
     * Consumer: functional interface with void method accepting a type (cf. Java 8 equivalent).
     *
     * @param <T> the argument type of the method
     */
    // TODO: Replace this with Java 8 type once we require Java 8
    public interface Consumer<T> {

        /**
         * Processes the given object.
         *
         * @param object the object to process
         */
        void accept(T object);
    }
}
