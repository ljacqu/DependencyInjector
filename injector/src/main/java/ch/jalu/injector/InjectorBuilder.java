package ch.jalu.injector;

import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.dependency.FactoryDependencyHandler;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;
import ch.jalu.injector.handlers.dependency.SingletonStoreDependencyHandler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.provider.ProviderHandlerImpl;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Configures and creates an {@link Injector}.
 */
public class InjectorBuilder {

    private InjectorConfig config;

    /**
     * Creates a new builder.
     */
    public InjectorBuilder() {
        config = new InjectorConfig();
    }

    /**
     * Returns all handlers that are added to the injector by default.
     *
     * @param rootPackage the root package of the project (to limit injection and scanning to)
     * @return all default handlers
     * @see #addDefaultHandlers(String)
     */
    public static List<Handler> createDefaultHandlers(String rootPackage) {
        InjectorUtils.checkNotNull(rootPackage, "root package may not be null");
        return new ArrayList<>(Arrays.asList(
            // (Annotation, Object) handler
            new SavedAnnotationsHandler(),
            // Provider / Factory / SingletonStore
            new ProviderHandlerImpl(),
            new FactoryDependencyHandler(),
            new SingletonStoreDependencyHandler(),
            // Instantiation provider
            new DefaultInjectionProvider(rootPackage),
            // PostConstruct
            new PostConstructMethodInvoker()));
    }

    /**
     * Creates all default handlers implementing {@link Handler#resolve}. Useful if you want to create your own
     * preconstruct (etc.) handlers but want to use the default instantiation providers.
     * <p>
     * Use {@link #createDefaultHandlers(String)} or {@link #addDefaultHandlers(String)} otherwise.
     *
     * @param rootPackage the root package of the project
     * @return default instantiation providers
     */
    public static List<Handler> createInstantiationProviders(String rootPackage) {
        return new ArrayList<>(Arrays.asList(
            new ProviderHandlerImpl(),
            new DefaultInjectionProvider(rootPackage)));
    }

    /**
     * Convenience method for adding all default handlers to the injector configuration.
     * To obtain an injector with all defaults, you can simply do:
     * <code>
     *   Injector injector = new InjectorBuilder().addDefaultHandlers("your.package.here").create();
     * </code>
     *
     * @param rootPackage the root package of the project
     * @return the builder
     */
    public InjectorBuilder addDefaultHandlers(String rootPackage) {
        return addHandlers(createDefaultHandlers(rootPackage));
    }

    /**
     * Add handlers to the config. Note that <b>the order of the handlers matters.</b> Handlers are
     * separated by their subtype and then executed in the order as provided.
     *
     * @param handlers the handlers to add to the injector
     * @return the builder
     */
    public InjectorBuilder addHandlers(Handler... handlers) {
        return addHandlers(Arrays.asList(handlers));
    }

    /**
     * Add handlers to the config. Note that <b>the order of the handlers matters.</b> Handlers are
     * separated by their subtype and then executed in the order as provided.
     *
     * @param handlers the handlers to add to the injector
     * @return the builder
     */
    public InjectorBuilder addHandlers(Collection<? extends Handler> handlers) {
        config.addHandlers(handlers);
        return this;
    }

    /**
     * Creates an injector with the configurations set to the builder.
     *
     * @return the injector
     */
    public Injector create() {
        return new InjectorImpl(config);
    }
}
