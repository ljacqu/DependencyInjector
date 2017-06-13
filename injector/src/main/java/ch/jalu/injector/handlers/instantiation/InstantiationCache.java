package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.InstantiationContext;
import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.context.UnresolvedInstantiationContext;
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

    protected Map<String, WeakReference<Instantiation>> entries = new ConcurrentHashMap<>();

    @Override
    public <T> Instantiation<? extends T> get(UnresolvedInstantiationContext<T> context) {
        return getInstantiation(context);
    }

    @Override
    public <T> T process(T object, ResolvedInstantiationContext<T> context) throws Exception {
        if (shouldCacheMethod(context) && getInstantiation(context) == null) {
            entries.put(context.getMappedClass().getCanonicalName(),
                new WeakReference<>(context.getInstantiation()));
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> Instantiation<? extends T> getInstantiation(InstantiationContext<T> context) {
        WeakReference<Instantiation> instantiation = entries.get(context.getMappedClass().getCanonicalName());
        return instantiation == null ? null : instantiation.get();
    }

    /**
     * Returns whether the instantiation method from the given context should be cached.
     *
     * @param context the context to process
     * @return true to cache the instantiation method, false otherwise
     */
    protected boolean shouldCacheMethod(ResolvedInstantiationContext<?> context) {
        return context.getResolutionType() == StandardResolutionType.REQUEST_SCOPED;
    }
}
