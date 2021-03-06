package ch.jalu.injector.testing.runner;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.TestClass;
import org.mockito.InjectMocks;
import org.mockito.Mockito;

/**
 * Validates that {@link ch.jalu.injector.testing.DelayedInjectionRunner} is used as intended.
 */
public class DelayedInjectionRunnerValidator extends RunListener {

    private final RunNotifier notifier;
    private final TestClass testClass;

    public DelayedInjectionRunnerValidator(RunNotifier notifier, TestClass testClass) {
        this.notifier = notifier;
        this.testClass = testClass;
    }

    @Override
    public void testFinished(Description description) throws Exception {
        try {
            Mockito.validateMockitoUsage();
            if (!testClass.getAnnotatedFields(InjectMocks.class).isEmpty()) {
                throw new IllegalStateException("Do not use @InjectMocks with the DelayedInjectionRunner:"
                    + " use @InjectDelayed or change runner");
            }
        } catch (Exception e) {
            notifier.fireTestFailure(new Failure(description, e));
        }
    }
}
