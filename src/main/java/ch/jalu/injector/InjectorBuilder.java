package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.annotations.AllInstancesAnnotationHandler;
import ch.jalu.injector.handlers.annotations.AllTypesAnnotationHandler;
import ch.jalu.injector.handlers.annotations.AnnotationHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructPackageValidator;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            // Annotations: @AllTypes and @AllInstances
            new AllTypesAnnotationHandler(rootPackage),
            new AllInstancesAnnotationHandler(rootPackage),
            // PostConstruct
            new PostConstructMethodInvoker()));
    }

    public InjectorBuilder addDefaultHandlers(String rootPackage) {
        return addHandlers(createDefaultHandlers(rootPackage));
    }

    public InjectorBuilder addHandlers(Handler... handlers) {
        return addHandlers(Arrays.asList(handlers));
    }

    public InjectorBuilder addHandlers(Iterable<Handler> handlers) {
        List<PreConstructHandler> preConstructHandlers = new ArrayList<>();
        List<AnnotationHandler> annotationHandlers = new ArrayList<>();
        List<PostConstructHandler> postConstructHandlers = new ArrayList<>();

        for (Handler handler : handlers) {
            if (handler instanceof PreConstructHandler) {
                preConstructHandlers.add((PreConstructHandler) handler);
            } else if (handler instanceof AnnotationHandler) {
                annotationHandlers.add((AnnotationHandler) handler);
            } else if (handler instanceof PostConstructHandler) {
                postConstructHandlers.add((PostConstructHandler) handler);
            } else {
                throw new InjectorException(
                    "Unknown Handler type. Handlers must implement a provided subtype", handler.getClass());
            }
        }

        config.addPreConstructHandlers(preConstructHandlers);
        config.addAnnotationHandlers(annotationHandlers);
        config.addPostConstructHandlers(postConstructHandlers);
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
