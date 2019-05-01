package ch.jalu.injector.testing;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.utils.InjectorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;

/**
 * Integration test for {@link DelayedInjectionExtension} with multiple {@link InjectDelayed} annotations.
 */
@ExtendWith(DelayedInjectionExtension.class)
public class MultipleDelayedInjectExtensionIntegrationTest {

    @InjectDelayed
    private GammaService gammaService;

    @InjectDelayed
    private BetaManager betaManager;

    @Mock
    private ProvidedClass providedClass;

    @Mock
    private AlphaService alphaService;

    @InjectDelayed
    private SampleInjectClass sampleInjectClass;

    @Mock
    private ClassWithAbstractDependency.AbstractDependency abstractDependency;

    @CustomAnnotation
    private String name = "Hello";


    @BeforeInjecting
    void beforeInjecting() {
        if (betaManager != null || gammaService != null || sampleInjectClass != null) {
            throw new IllegalStateException("Field with @InjectDelayed is not null in @BeforeInjecting method");
        }
        InjectorUtils.checkNoNullValues(alphaService, providedClass, abstractDependency);
        given(alphaService.getProvidedClass()).willReturn(providedClass);
    }

    @BeforeEach
    void before() {
        InjectorUtils.checkNoNullValues(gammaService, betaManager, providedClass,
            alphaService, sampleInjectClass, alphaService);
    }

    @Test
    public void shouldSetUpProperly() {
        // If we're in here it means we could set up successfully; some validation has already been done
        // in the Before methods, but we can also check that we only have one instance per class
        assertThat(sampleInjectClass.getStringField(), equalTo(name));
        assertThat(sampleInjectClass.getAlphaService().getProvidedClass(), sameInstance(providedClass));
        assertThat(sampleInjectClass.getProvidedClass(), sameInstance(providedClass));
        assertThat(betaManager.getDependencies()[1], sameInstance(gammaService));
    }
}
