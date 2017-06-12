package ch.jalu.injector.samples;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sample class - tests various situations for the instantiation fallback.
 */
public abstract class InstantiationFallbackClasses {

    public static final class FallbackClass {
        // No @Inject annotations, public no-args constructor
    }

    public static final class HasFallbackDependency {
        @Inject
        private FallbackClass fallbackClass;

        @Inject
        private GammaService gammaService;

        public GammaService getGammaService() {
            return gammaService;
        }

        public FallbackClass getFallbackDependency() {
            return fallbackClass;
        }
    }

    public static final class InvalidNoArgConstructorClass {
        private InvalidNoArgConstructorClass() {
            // no-args constructor must be public for public classes
        }
    }

    public static final class ClassWithPostConstruct {
        @PostConstruct
        public void postConstructMethod() {
            // --
        }
    }

}
