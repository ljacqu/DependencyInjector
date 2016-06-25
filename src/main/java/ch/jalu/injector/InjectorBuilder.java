package ch.jalu.injector;

import ch.jalu.injector.handlers.annotations.AnnotationHandler;

import java.util.Arrays;

/**
 * Configures and creates an {@link Injector}.
 */
public class InjectorBuilder {

    private InjectorConfig config;

    public InjectorBuilder() {
        config = new InjectorConfig();
    }

    /**
     * Sets the package under which classes may be instantiated. Supply your project's package so in case
     * the injector stumbles upon a class with external dependencies, it will throw an exception instead of
     * trying to instantiate external classes (typically not desired).
     * <p>
     * To allow instantiation regardless of package, pass an empty string (not recommended).
     *
     * @param rootPackage the root package of the application
     * @return the builder
     */
    @Deprecated // Subject to change in future versions
    public InjectorBuilder setAllowedPackages(String rootPackage) {
        config.setRootPackage(rootPackage);
        return this;
    }

    public InjectorBuilder addAnnotationHandlers(AnnotationHandler... annotationHandler) {
        config.addAnnotationHandlers(Arrays.asList(annotationHandler));
        return this;
    }

    public InjectorBuilder setAnnotationHandlers(AnnotationHandler... annotationHandlers) {
        config.setAnnotationHandlers(Arrays.asList(annotationHandlers));
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
