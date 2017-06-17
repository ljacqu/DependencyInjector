package ch.jalu.injector;

import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.SimpleObjectResolution;
import ch.jalu.injector.utils.InjectorUtils;

/**
 * Utilities for handling Instantiation objects.
 */
public final class InstantiationTestHelper {

    private InstantiationTestHelper() {
    }

    public static Object unwrapFromSimpleResolution(Instantiation<?> instantiation) {
        if (instantiation instanceof SimpleObjectResolution<?>) {
            InjectorUtils.checkArgument(instantiation.getDependencies().isEmpty(),
                "Expected list of required dependencies to be empty!");
            return instantiation.instantiateWith();
        }
        throw new IllegalStateException("Expected object of type '" + SimpleObjectResolution.class
            + "' but received object of type '" + instantiation.getClass() + "'");
    }
}
