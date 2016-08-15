package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.utils.InjectorUtils;

import javax.annotation.Nullable;

/**
 * Instantiation provider for instantiations that happen on the class directly,
 * i.e. limited to classes which are instantiable.
 */
public abstract class DirectInstantiationProvider implements InstantiationProvider {

    @Override
    public final <T> Instantiation<T> get(Class<T> clazz) {
        if (InjectorUtils.canInstantiate(clazz)) {
            return safeGet(clazz);
        }
        return null;
    }

    /**
     * Gets the instantiation for the class or null if unavailable. This method
     * is only called with classes which can be instantiated.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return the instantiation, or null if not applicable
     */
    @Nullable
    protected abstract <T> Instantiation<T> safeGet(Class<T> clazz);

}
