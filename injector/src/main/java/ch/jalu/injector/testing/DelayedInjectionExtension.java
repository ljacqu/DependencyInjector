package ch.jalu.injector.testing;

import ch.jalu.injector.testing.extension.DelayedInjectionRunnerValidator;
import ch.jalu.injector.testing.extension.RunBeforeInjectings;
import ch.jalu.injector.testing.extension.RunDelayedInjects;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockitoAnnotations;

public class DelayedInjectionExtension implements BeforeEachCallback, AfterAllCallback {

    @Override
    public void beforeEach(ExtensionContext context) {
        Object instance = context.getRequiredTestInstance();
        MockitoAnnotations.initMocks(instance);

        new RunBeforeInjectings(instance).evaluate();
        new RunDelayedInjects(instance).evaluate();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        new DelayedInjectionRunnerValidator(context.getRequiredTestClass()).testFinished();
    }
}
