package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithAnnotations;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InvalidClass;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Size;
import org.junit.Test;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link ConstructorInjection}.
 */
public class ConstructorInjectionTest {

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnDependencies() {
        // given
        Instantiation<ClassWithAnnotations> injection = ConstructorInjection.provide(ClassWithAnnotations.class).get();

        // when
        Class<?>[] dependencies = injection.getDependencies();
        Class<?>[] annotations = injection.getDependencyAnnotations();

        // then
        assertThat(dependencies, arrayContaining(int.class, GammaService.class, long.class));
        assertThat(annotations, arrayContaining((Class<?>) Size.class, null, Duration.class));
    }

    @Test
    public void shouldInstantiate() {
        // given
        GammaService gammaService = new GammaService(
            AlphaService.newInstance(new ProvidedClass("")));
        Instantiation<ClassWithAnnotations> injection = ConstructorInjection.provide(ClassWithAnnotations.class).get();

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
        Instantiation<ClassWithAnnotations> injection = ConstructorInjection.provide(ClassWithAnnotations.class).get();

        // when / then
        injection.instantiateWith(-112, null, 12L);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowUponInstantiationError() {
        // given
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        Instantiation<InvalidClass> injection = ConstructorInjection.provide(InvalidClass.class).get();

        // when
        injection.instantiateWith(alphaService, 5);
    }

    @Test
    public void shouldReturnNullForNoConstructorInjection() {
        // given / when
        Instantiation<BetaManager> injection = ConstructorInjection.provide(BetaManager.class).get();

        // then
        assertThat(injection, nullValue());
    }
}
