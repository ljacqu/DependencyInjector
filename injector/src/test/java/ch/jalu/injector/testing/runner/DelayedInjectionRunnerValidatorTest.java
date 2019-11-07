package ch.jalu.injector.testing.runner;

import ch.jalu.injector.Injector;
import ch.jalu.injector.testing.DelayedInjectionRunnerIntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.TestClass;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link DelayedInjectionRunnerValidator}.
 */
public class DelayedInjectionRunnerValidatorTest {

    @InjectMocks
    private Injector injector; // this field is null (no runner set) and serves as test setup

    @Test
    public void shouldValidateSuccessfully() throws Exception {
        // given
        RunNotifier notifier = mock(RunNotifier.class);
        TestClass testClass = new TestClass(DelayedInjectionRunnerIntegrationTest.class);
        DelayedInjectionRunnerValidator validator = new DelayedInjectionRunnerValidator(notifier, testClass);
        Description description = mock(Description.class);

        // when
        validator.testFinished(description);

        // then
        // nothing happens: successful validation
        verifyZeroInteractions(notifier, description);
    }

    @Test
    public void shouldValidateUnsuccessfullyForInjectMocksPresence() throws Exception {
        // given
        RunNotifier notifier = mock(RunNotifier.class);
        TestClass testClass = new TestClass(getClass());
        DelayedInjectionRunnerValidator validator = new DelayedInjectionRunnerValidator(notifier, testClass);
        Description description = mock(Description.class);

        // when
        validator.testFinished(description);

        // then
        ArgumentCaptor<Failure> captor = ArgumentCaptor.forClass(Failure.class);
        verify(notifier).fireTestFailure(captor.capture());
        Failure failure = captor.getValue();
        assertThat(failure.getMessage(), containsString("Do not use @InjectMocks"));
    }

}