package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

/**
 * Simple instantiation method that creates an object via a known provider object.
 *
 * @param <T> the type of the class to create
 */
public class ProviderByObject<T> implements Instantiation<T> {

    private final Provider<? extends T> provider;

    /**
     * Constructor.
     *
     * @param provider the provider to use
     */
    public ProviderByObject(Provider<? extends T> provider) {
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

    public Provider<? extends T> getProvider() {
        return provider;
    }
}
