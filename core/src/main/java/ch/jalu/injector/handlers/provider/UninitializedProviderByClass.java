package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.inject.Provider;
import java.util.Collections;
import java.util.List;

/**
 * Instantiation method for a provider class of a given method.
 * <p>
 * This is a simple instantiation method implementation that declares the given provider class
 * as dependency, letting the injector figure out what dependencies the class has and how to
 * construct it.
 *
 * @param <T> the type the uninitialized class produces
 */
public class UninitializedProviderByClass<T> implements Instantiation<Provider<T>> {

    private final Class<? extends Provider<? extends T>> providerClass;

    public UninitializedProviderByClass(Class<? extends Provider<? extends T>> providerClass) {
        this.providerClass = providerClass;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        return Collections.singletonList(
            new DependencyDescription(providerClass, null));
    }

    @Override
    public Provider<T> instantiateWith(Object... values) {
        if (values.length == 1 && values[0] instanceof Provider<?>) {
            return (Provider<T>) values[0];
        }
        throw new InjectorException("Provider is required as argument");
    }
}
