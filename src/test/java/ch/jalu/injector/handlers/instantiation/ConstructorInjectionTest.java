package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithAnnotations;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InjectOnDifferentMembersClass;
import ch.jalu.injector.samples.InvalidClass;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Size;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.List;

import static ch.jalu.injector.TestUtils.annotationOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ConstructorInjection} and {@link ConstructorInjectionProvider}.
 */
public class ConstructorInjectionTest {
    
    private ConstructorInjectionProvider provider = new ConstructorInjectionProvider();

    @Test
    public void shouldReturnDependencies() {
        // given
        ConstructorInjection<ClassWithAnnotations> injection = provider.get(ClassWithAnnotations.class);

        // when
        List<DependencyDescription> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(3));
        assertDependencyEqualTo(dependencies.get(0), int.class, Size.class);
        assertDependencyEqualTo(dependencies.get(1), GammaService.class, null);
        assertDependencyEqualTo(dependencies.get(2), long.class, Duration.class);
        assertThat(((Size) dependencies.get(0).getAnnotations()[0]).value(), equalTo("box"));
    }

    @Test
    public void shouldInstantiate() {
        // given
        GammaService gammaService = new GammaService(
            AlphaService.newInstance(new ProvidedClass("")));
        Instantiation<ClassWithAnnotations> injection = provider.get(ClassWithAnnotations.class);

        // when
        ClassWithAnnotations instance = injection.instantiateWith(-112, gammaService, 19L);

        // then
        assertThat(instance, not(nullValue()));
        assertThat(instance.getSize(), equalTo(-112));
        assertThat(instance.getGammaService(), equalTo(gammaService));
        assertThat(instance.getDuration(), equalTo(19L));
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForNullValue() {
        // given
        Instantiation<ClassWithAnnotations> injection = provider.get(ClassWithAnnotations.class);

        // when / then
        injection.instantiateWith(-112, null, 12L);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowUponInstantiationError() {
        // given
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        Instantiation<InvalidClass> injection = provider.get(InvalidClass.class);

        // when
        injection.instantiateWith(alphaService, 5);
    }

    @Test
    public void shouldReturnNullForNoConstructorInjection() {
        // given / when
        Instantiation<BetaManager> injection = provider.get(BetaManager.class);

        // then
        assertThat(injection, nullValue());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForOtherInjectAnnotationsPresent() {
        // Should throw - @Inject on field as well as constructors
        provider.get(InjectOnDifferentMembersClass.class);
    }

    @SuppressWarnings("unchecked")
    private static void assertDependencyEqualTo(DependencyDescription dependency,
                                                Class<?> type, Class<?> annotationType) {
        assertThat(dependency.getType(), Matchers.<Class<?>>equalTo(type));
        if (annotationType != null) {
            assertThat(dependency.getAnnotations(), arrayContaining(annotationOf(annotationType)));
        }
    }
}
