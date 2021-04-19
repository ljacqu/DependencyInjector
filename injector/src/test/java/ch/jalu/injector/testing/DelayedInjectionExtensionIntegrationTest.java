package ch.jalu.injector.testing;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

/**
 * Some sample tests with {@link DelayedInjectionExtension} test extension for JUnit 5.
 */
@ExtendWith(DelayedInjectionExtension.class)
class DelayedInjectionExtensionIntegrationTest {

    private List<String> executionOrder = new ArrayList<>();

    @InjectDelayed
    private SampleInjectClass sampleInjectClass;

    @Mock
    private AlphaService alphaService;

    @Mock
    private ClassWithAbstractDependency.AbstractDependency abstractDependency;

    @CustomAnnotation
    @Mock
    private GammaService gammaService;

    @CustomAnnotation
    private String stringFieldSample = "Test value";

    @BeforeInjecting
    void initBeforeInject() {
        if (alphaService == null || abstractDependency == null) {
            throw new IllegalStateException("@Mock fields should be initialized in @BeforeInject");
        }
        if (sampleInjectClass != null) {
            throw new IllegalStateException("@InjectDelayed field should be null in @BeforeInject method");
        }
        given(alphaService.getProvidedClass()).willReturn(mock(ProvidedClass.class));
        executionOrder.add("BeforeInjecting");
    }

    @BeforeEach
    void runBeforeMethod() {
        if (alphaService == null || abstractDependency == null || sampleInjectClass == null) {
            throw new IllegalStateException("Found null field annotated with @InjectDelayed or @Mock. "
                + "This should not be the case in the @BeforeEach method.");
        }
        executionOrder.add("BeforeEach");
    }

    @Test
    void shouldHaveInjectedProperly() {
        // The providedClass is set in a @PostConstruct method, so check that this worked
        ProvidedClass providedClass = sampleInjectClass.getProvidedClass();
        assertThat(providedClass, not(nullValue()));

        // Check that the string field is set
        assertThat(sampleInjectClass.getStringField(), equalTo(stringFieldSample));

        // Otherwise not much to check, having landed here means the test was run successfully.
        // @BeforeInjecting and @BeforeEach methods check that they were run under the expected conditions.
        assertThat(executionOrder, contains("BeforeInjecting", "BeforeEach"));
    }

}