package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectContext;
import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.context.UnresolvedContext;
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

    protected Map<Class, WeakReference<Instantiation>> entries = new ConcurrentHashMap<>();

    @Override
    public Instantiation<?> get(UnresolvedContext context) {
        return getInstantiation(context);
    }

    @Override
    public <T> T postProcess(T object, ResolvedContext context) {
        if (shouldCacheMethod(context) && getInstantiation(context) == null) {
            // TODO #48: Refine this to go over the entire object identifier
            entries.put(context.getIdentifier().getType(),
                new WeakReference<>(context.getInstantiation()));
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> Instantiation<? extends T> getInstantiation(ObjectContext context) {
        WeakReference<Instantiation> instantiation = entries.get(context.getIdentifier().getType());
        return instantiation == null ? null : instantiation.get();
    }

    /**
     * Returns whether the instantiation method from the given context should be cached.
     *
     * @param context the context to process
     * @return true to cache the instantiation method, false otherwise
     */
    protected boolean shouldCacheMethod(ResolvedContext context) {
        return context.getResolutionType() == StandardResolutionType.REQUEST_SCOPED;
    }
}
