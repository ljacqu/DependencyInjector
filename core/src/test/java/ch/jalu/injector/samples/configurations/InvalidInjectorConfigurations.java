package ch.jalu.injector.samples.configurations;

import ch.jalu.injector.annotations.Provides;
import ch.jalu.injector.config.InjectorConfiguration;
import ch.jalu.injector.samples.ProvidedClass;

/**
 * Collection of invalid configurations.
 */
public class InvalidInjectorConfigurations {

    public static final class ConfigurationWithVoidReturnType implements InjectorConfiguration {
        @Provides
        public ProvidedClass initProvidedClass() {
            return new ProvidedClass("");
        }

        @Provides
        public void initBetaManager() {
            // invalid return type
        }
    }

    public static final class ConfigurationWithParameters implements InjectorConfiguration {
        @Provides
        public ProvidedClass initProvidedClass(boolean b) {
            return new ProvidedClass("");
        }
    }
}
