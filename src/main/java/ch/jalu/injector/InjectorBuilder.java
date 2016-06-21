package ch.jalu.injector;

/**
 * Configures and creates an {@link Injector}.
 */
public class InjectorBuilder {

    private String[] allowedPackages;

    public InjectorBuilder() {
    }

    /**
     * Sets the packages in which classes may be instantiated. Supply your project's package(s) so in case
     * the injector stumbles upon a class with external dependencies, it will throw an exception instead of
     * trying to instantiate external classes (typically not desired).
     * <p>
     * <b>You must supply packages</b>, otherwise the injector will throw an exception for any class it is
     * requested to instantiate. To allow instantiation regardless of package, supply an empty string (not recommended).
     *
     * @param packages the packages in which classes will be instantiated automatically
     * @return the builder
     */
    public InjectorBuilder setAllowedPackages(String... packages) {
        allowedPackages = packages;
        return this;
    }

    /**
     * Creates an injector with the configurations set to the builder.
     *
     * @return the injector
     */
    public Injector create() {
        if (allowedPackages == null) {
            allowedPackages = new String[0];
        }
        return new InjectorImpl(allowedPackages);
    }

}
