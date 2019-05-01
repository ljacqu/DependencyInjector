package ch.jalu.injector.testing.extension;

import org.mockito.InjectMocks;
import org.mockito.Mockito;

import static ch.jalu.injector.testing.extension.ExtensionUtils.getAnnotatedFields;

/**
 * Validates that {@link ch.jalu.injector.testing.DelayedInjectionRunner} is used as intended.
 */
public class DelayedInjectionRunnerValidator {

    private final Class<?> testClass;

    public DelayedInjectionRunnerValidator(Class<?> testClass) {
        this.testClass = testClass;
    }

    public void testFinished() {
        Mockito.validateMockitoUsage();
        if (!getAnnotatedFields(testClass, InjectMocks.class).isEmpty()) {
            throw new IllegalStateException("Do not use @InjectMocks with the DelayedInjectionRunner:"
                + " use @InjectDelayed or change runner");
        }
    }
}
