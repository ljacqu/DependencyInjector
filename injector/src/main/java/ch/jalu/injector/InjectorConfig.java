package ch.jalu.injector;

import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Injector configuration.
 */
public class InjectorConfig {

    private List<Handler> handlers = new ArrayList<>();

    /**
     * Use the {@link InjectorBuilder} instead of instantiating this.
     */
    protected InjectorConfig() {
    }

    public void addHandlers(Collection<? extends Handler> handlers) {
        InjectorUtils.checkNotNull(handlers, null);
        this.handlers.addAll(handlers);
    }

    public List<Handler> getHandlers() {
        return handlers;
    }
}
