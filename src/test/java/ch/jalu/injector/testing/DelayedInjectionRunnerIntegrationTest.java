package ch.jalu.injector.testing;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.samples.ProvidedClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Some sample tests with {@link DelayedInjectionRunner} as test runner.
 */
@RunWith(DelayedInjectionRunner.class)
public class DelayedInjectionRunnerIntegrationTest {

    @InjectDelayed
    private SampleInjectClass sampleInjectClass;

    @Mock
    private AlphaService alphaService;

    @Mock
    private ClassWithAbstractDependency.AbstractDependency abstractDependency;

    @BeforeInjecting
    public void initBeforeInject() {
        if (alphaService == null || abstractDependency == null) {
            throw new IllegalStateException("@Mock fields should be initialized in @BeforeInject");
        }
        if (sampleInjectClass != null) {
            throw new IllegalStateException("@InjectDelayed field should be null in @BeforeInject method");
        }
        given(alphaService.getProvidedClass()).willReturn(mock(ProvidedClass.class));
    }

    @Before
    public void runBeforeMethod() {
        if (alphaService == null || abstractDependency == null || sampleInjectClass == null) {
            throw new IllegalStateException("Found null field annotated with @InjectDelayed or @Mock. "
                + "This should not be the case in the @Before method.");
        }
    }

    @Test
    public void shouldHaveInjectedProperly() {
        // The providedClass is set in a @PostConstruct method, so check that this worked
        ProvidedClass providedClass = sampleInjectClass.getProvidedClass();
        assertThat(providedClass, not(nullValue()));

        // Otherwise not much to check, having landed here means the test was run successfully.
        // @BeforeInjecting and @Before methods check that they were run under the expected conditions.
    }

}