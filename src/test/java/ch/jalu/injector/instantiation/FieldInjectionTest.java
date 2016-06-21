package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BadFieldInjection;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithAnnotations;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.FieldInjectionWithAnnotations;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InvalidStaticFieldInjection;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Size;
import org.junit.Test;

import javax.inject.Inject;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link FieldInjection}.
 */
public class FieldInjectionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnDependencies() {
        // given
        FieldInjection<FieldInjectionWithAnnotations> injection =
            FieldInjection.provide(FieldInjectionWithAnnotations.class).get();

        // when
        Class<?>[] dependencies = injection.getDependencies();
        Class<?>[] annotations = injection.getDependencyAnnotations();

        // then
        assertThat(dependencies, arrayContaining(BetaManager.class, int.class, long.class, ClassWithAnnotations.class));
        assertThat(annotations, arrayContaining((Class<?>) null, Size.class, Duration.class, null));
    }

    @Test
    public void shouldInstantiateClass() {
        // given
        FieldInjection<BetaManager> injection = FieldInjection.provide(BetaManager.class).get();
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);

        // when
        BetaManager betaManager = injection.instantiateWith(providedClass, gammaService, alphaService);

        // then
        assertThat(betaManager, not(nullValue()));
        assertThat(betaManager.getDependencies(), arrayContaining(providedClass, gammaService, alphaService));
    }

    @Test
    public void shouldProvideNullForImpossibleFieldInjection() {
        // given / when
        FieldInjection<BadFieldInjection> injection = FieldInjection.provide(BadFieldInjection.class).get();

        // then
        assertThat(injection, nullValue());
    }

    @Test(expected = InjectorException.class)
    public void shouldForwardExceptionDuringInstantiation() {
        // given
        FieldInjection<ThrowingConstructor> injection = FieldInjection.provide(ThrowingConstructor.class).get();

        // when / when
        injection.instantiateWith(new ProvidedClass(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForInvalidFieldValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);
        FieldInjection<BetaManager> injection = FieldInjection.provide(BetaManager.class).get();

        // when / then
        // Correct order is provided, gamma, alpha
        injection.instantiateWith(providedClass, alphaService, gammaService);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForNullValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        FieldInjection<BetaManager> injection = FieldInjection.provide(BetaManager.class).get();

        // when / then
        // Correct order is provided, gamma, alpha
        injection.instantiateWith(providedClass, null, alphaService);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForStaticFieldInjection() {
        // given / when / then
        FieldInjection.provide(InvalidStaticFieldInjection.class).get();
    }

    @Test
    public void shouldNotReturnFieldInjectionForZeroInjectFields() {
        // given / when
        Instantiation<NoInjectionClass> injection = FieldInjection.provide(NoInjectionClass.class).get();

        // then
        assertThat(injection, nullValue());
    }


    private static final class ThrowingConstructor {
        @Inject
        private ProvidedClass providedClass;

        public ThrowingConstructor() {
            throw new UnsupportedOperationException("Exception in constructor");
        }
    }

    private static final class NoInjectionClass {

    }
}
