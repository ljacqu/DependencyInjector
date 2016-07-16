package ch.jalu.injector.instantiation;

import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.FieldInjection;
import ch.jalu.injector.handlers.instantiation.FieldInjectionProvider;
import ch.jalu.injector.handlers.instantiation.Instantiation;
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
import org.hamcrest.Matchers;
import org.junit.Test;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.List;

import static ch.jalu.injector.TestUtils.annotationOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link FieldInjection} and {@link FieldInjectionProvider}.
 */
public class FieldInjectionTest {
    
    private FieldInjectionProvider provider = new FieldInjectionProvider();

    @Test
    public void shouldReturnDependencies() {
        // given
        FieldInjection<FieldInjectionWithAnnotations> injection =
            provider.get(FieldInjectionWithAnnotations.class);

        // when
        List<DependencyDescription> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(4));
        assertDependencyEqualTo(dependencies.get(0), BetaManager.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(1), int.class, Inject.class, Size.class);
        assertDependencyEqualTo(dependencies.get(2), long.class, Duration.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(3), ClassWithAnnotations.class, Inject.class);

        assertThat(((Size) dependencies.get(1).getAnnotations()[1]).value(), equalTo("chest"));
    }

    @Test
    public void shouldInstantiateClass() {
        // given
        FieldInjection<BetaManager> injection = provider.get(BetaManager.class);
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
        FieldInjection<BadFieldInjection> injection = provider.get(BadFieldInjection.class);

        // then
        assertThat(injection, nullValue());
    }

    @Test(expected = InjectorException.class)
    public void shouldForwardExceptionDuringInstantiation() {
        // given
        FieldInjection<ThrowingConstructor> injection = provider.get(ThrowingConstructor.class);

        // when / when
        injection.instantiateWith(new ProvidedClass(""));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForInvalidFieldValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);
        FieldInjection<BetaManager> injection = provider.get(BetaManager.class);

        // when / then
        // Correct order is provided, gamma, alpha
        injection.instantiateWith(providedClass, alphaService, gammaService);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForNullValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        FieldInjection<BetaManager> injection = provider.get(BetaManager.class);

        // when / then
        // Correct order is provided, gamma, alpha
        injection.instantiateWith(providedClass, null, alphaService);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForStaticFieldInjection() {
        // given / when / then
        provider.get(InvalidStaticFieldInjection.class);
    }

    @Test
    public void shouldNotReturnFieldInjectionForZeroInjectFields() {
        // given / when
        Instantiation<NoInjectionClass> injection = provider.get(NoInjectionClass.class);

        // then
        assertThat(injection, nullValue());
    }

    @Test
    public void shouldNotScanClassWithNoFieldScan() {
        // given / when
        FieldInjection<NoFieldScanClass> injection = provider.get(NoFieldScanClass.class);

        // then
        assertThat(injection, nullValue());
    }

    @SafeVarargs
    private static void assertDependencyEqualTo(DependencyDescription dependency, Class<?> type,
                                                Class<? extends Annotation>... annotations) {
        assertThat(dependency.getType(), Matchers.<Class<?>>equalTo(type));
        assertThat(dependency.getAnnotations(), arrayWithSize(annotations.length));

        for (int i = 0; i < annotations.length; ++i) {
            assertThat(dependency.getAnnotations()[i], annotationOf(annotations[i]));
        }
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

    @NoFieldScan
    private static final class NoFieldScanClass {
        @Inject
        private ProvidedClass providedClass;
    }
}
