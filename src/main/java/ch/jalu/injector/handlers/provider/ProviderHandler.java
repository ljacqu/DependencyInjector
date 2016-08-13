package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.Handler;

import javax.inject.Provider;

/**
 * .
 */
public interface ProviderHandler extends Handler {

    <T> void onProvider(Class<T> clazz, Provider<? extends T> provider) throws Exception;

    <T, P extends Provider<? extends T>> void onProviderClass(Class<T> clazz, Class<P> providerClass) throws Exception;

}
