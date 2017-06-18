package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.handlers.Handler;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Primitive "cache" for instantiation methods. It is recommended to use a more suitable
 * cache structure instead, e.g. with Guava's CacheBuilder. As such, this handler is not
 * included by default.
 */
public class InstantiationCache implements Handler {

    protected Map<Class, WeakReference<Resolution>> entries = new ConcurrentHashMap<>();

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        return getInstantiation(context);
    }

    @Override
    public <T> T postProcess(T object, ResolutionContext context, Resolution<?> resolution) {
        if (shouldCacheMethod(context) && getInstantiation(context) == null) {
            // TODO #48: Refine this to go over the entire object identifier
            entries.put(context.getIdentifier().getTypeAsClass(), new WeakReference<>(resolution));
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> Resolution<? extends T> getInstantiation(ResolutionContext context) {
        WeakReference<Resolution> instantiation = entries.get(context.getIdentifier().getTypeAsClass());
        return instantiation == null ? null : instantiation.get();
    }

    /**
     * Returns whether the instantiation method from the given context should be cached.
     *
     * @param context the context to process
     * @return true to cache the instantiation method, false otherwise
     */
    protected boolean shouldCacheMethod(ResolutionContext context) {
        return context.getIdentifier().getResolutionType() == StandardResolutionType.REQUEST_SCOPED;
    }
}
