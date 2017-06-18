package ch.jalu.injector;

import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.Size;
import ch.jalu.injector.utils.InjectorUtils;

/**
 * Utilities for types specific to this project.
 */
public final class InjectorTestHelper {

    private InjectorTestHelper() {
    }

    public static Object unwrapFromSimpleResolution(Resolution<?> resolution) {
        if (resolution instanceof SimpleResolution<?>) {
            InjectorUtils.checkArgument(resolution.getDependencies().isEmpty(),
                "Expected list of required dependencies to be empty!");
            return resolution.instantiateWith();
        }
        throw new IllegalStateException("Expected object of type '" + SimpleResolution.class
            + "' but received object of type '" + resolution.getClass() + "'");
    }

    public static Size newSizeAnnotation(String value) {
        return new Size() {
            @Override
            public Class<Size> annotationType() {
                return Size.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    public static Duration newDurationAnnotation() {
        return new Duration() {
            @Override
            public Class<Duration> annotationType() {
                return Duration.class;
            }
        };
    }
}
