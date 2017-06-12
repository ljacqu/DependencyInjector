package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.Handler;

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
     * Processes the given provider class.
     *
     * @param clazz the class to associate the provider class with
     * @param providerClass the provider class
     * @param <T> the class' type
     * @param <P> the provider class' type
     * @throws Exception for unsuccessful validation, etc.
     */
    <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz, Class<P> providerClass) throws Exception;

}
