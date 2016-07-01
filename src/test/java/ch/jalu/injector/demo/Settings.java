package ch.jalu.injector.demo;

/**
 * Settings instance.
 * <p>
 * This is a simple class without any dependencies. The injector can create it with the
 * {@link ch.jalu.injector.handlers.instantiation.InstantiationFallback}, which allows to create
 * classes that have a public no-args constructor.
 */
public class Settings {

    /** Language code to use. Supported: en, gsw, fr. */
    private final String language = "en";

    /** Number of total digits (NOT just decimals). */
    private final int precision = 7;


    public int getPrecision() {
        return precision;
    }

    public String getLanguage() {
        return language;
    }

}
