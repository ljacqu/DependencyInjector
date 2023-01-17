package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.utils.InjectorUtils;

import javax.annotation.Nullable;

/**
 * Instantiation provider for instantiations that happen on the class directly,
 * i.e. limited to classes which are instantiable.
 */
public abstract class DirectInstantiationProvider implements Handler {

    @Override
    public final Resolution<?> resolve(ResolutionContext context) {
        final Class<?> clazz = context.getIdentifier().getTypeAsClass();
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
     * @param <T> the class's type
     * @return the instantiation, or null if not applicable
     */
    @Nullable
    protected abstract <T> Resolution<T> safeGet(Class<T> clazz);

}
