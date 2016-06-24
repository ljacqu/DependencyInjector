package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.*;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.*;
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
        Annotation[][] annotations = injection.getDependencyAnnotations();

        // then
        assertThat(dependencies, arrayContaining(int.class, GammaService.class, long.class));
        //FIXME assertThat(annotations, arrayContaining((Class<?>) Size.class, null, Duration.class));
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
