package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.inject.Provider;

/**
 * Handles {@link Provider} objects and classes supplied to the injector.
 */
public interface ProviderHandler extends Handler {

    /**
     * Processes the given provider.
     *
     * @param clazz the class to associate the provider with
     * @param provider the provider
     * @param <T> the class' type
     * @throws Exception for unsuccessful validation, etc.
     */
    <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) throws Exception;

    /**
     * Processes the given instantiation of a provider for the given class.
     *
     * @param clazz the class to associate the future provider with
     * @param providerInstantiation the instantiation method of the provider
     * @param <T> the class' type
     * @throws Exception upon unsuccessful operation, etc.
     */
    <T> void onProvider(Class<T> clazz, Instantiation<? extends Provider<? extends T>> providerInstantiation)
                        throws Exception;

}
