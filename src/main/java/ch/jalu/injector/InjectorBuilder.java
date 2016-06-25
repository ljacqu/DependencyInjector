package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.dependency.AllInstancesAnnotationHandler;
import ch.jalu.injector.handlers.dependency.AllTypesAnnotationHandler;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.ConstructorInjectionProvider;
import ch.jalu.injector.handlers.instantiation.FieldInjectionProvider;
import ch.jalu.injector.handlers.instantiation.InstantiationFallbackProvider;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructPackageValidator;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Configures and creates an {@link Injector}.
 */
public class InjectorBuilder {

    private InjectorConfig config;

    public InjectorBuilder() {
        config = new InjectorConfig();
    }

    public List<Handler> createDefaultHandlers(String rootPackage) {
        InjectorUtils.checkNotNull(rootPackage, "root package may not be null", String.class);
        return new ArrayList<>(Arrays.asList(
            // PreConstruct
            new PreConstructPackageValidator(rootPackage),
            // Instantiation providers
            new ConstructorInjectionProvider(),
            new FieldInjectionProvider(),
            new InstantiationFallbackProvider(),
            // Dependencies: @AllTypes and @AllInstances
            new AllTypesAnnotationHandler(rootPackage),
            new AllInstancesAnnotationHandler(rootPackage),
            // PostConstruct
            new PostConstructMethodInvoker()));
    }

    public List<InstantiationProvider> createInstantiationProviders() {
        return new ArrayList<>(Arrays.asList(
            new ConstructorInjectionProvider(),
            new FieldInjectionProvider(),
            new InstantiationFallbackProvider()));
    }

    public InjectorBuilder addDefaultHandlers(String rootPackage) {
        return addHandlers(createDefaultHandlers(rootPackage));
    }

    public InjectorBuilder addHandlers(Handler... handlers) {
        return addHandlers(Arrays.asList(handlers));
    }

    public InjectorBuilder addHandlers(Iterable<? extends Handler> handlers) {
        HandlerCollector collector = new HandlerCollector(
            PreConstructHandler.class, InstantiationProvider.class,
            DependencyHandler.class, PostConstructHandler.class);

        for (Handler handler : handlers) {
            collector.process(handler);
        }

        config.addPreConstructHandlers(collector.getList(PreConstructHandler.class));
        config.addInstantiationProviders(collector.getList(InstantiationProvider.class));
        config.addDependencyHandlers(collector.getList(DependencyHandler.class));
        config.addPostConstructHandlers(collector.getList(PostConstructHandler.class));
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

    @SuppressWarnings("unchecked")
    private static final class HandlerCollector {

        private final Map<Class<? extends Handler>, List<? extends Handler>> handlersByType = new HashMap<>();
        private final Class<? extends Handler>[] subtypes;

        @SafeVarargs
        public HandlerCollector(Class<? extends Handler>... subtypes) {
            this.subtypes = subtypes;
            for (Class<? extends Handler> subtype : subtypes) {
                handlersByType.put(subtype, new ArrayList<Handler>());
            }
        }

        public void process(Handler handler) {
            boolean foundSubtype = false;
            for (Class<? extends Handler> subtype : subtypes) {
                foundSubtype |= addHandler(subtype, handler);
            }
            if (!foundSubtype) {
                throw new InjectorException(
                    "Unknown Handler type. Handlers must implement a known subtype", handler.getClass());
            }
        }

        public <T extends Handler> List<T> getList(Class<T> clazz) {
            return (List<T>) handlersByType.get(clazz);
        }

        private <T extends Handler> boolean addHandler(Class<T> clazz, Handler handler) {
            if (clazz.isInstance(handler)) {
                getList(clazz).add((T) handler);
                return true;
            }
            return false;
        }
    }

}
