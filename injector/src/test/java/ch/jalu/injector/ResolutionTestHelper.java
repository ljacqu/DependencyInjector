package ch.jalu.injector;

import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.utils.InjectorUtils;

/**
 * Utilities for handling {@link Resolution} objects.
 */
public final class ResolutionTestHelper {

    private ResolutionTestHelper() {
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
}
