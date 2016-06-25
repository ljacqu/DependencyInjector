package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InstantiationFallbackClasses;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link InstantiationFallback}.
 */
public class InstantiationFallbackTest {

    @Test
    public void shouldInstantiateClass() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.FallbackClass.class).get();

        // when
        InstantiationFallbackClasses.FallbackClass result = instantiation.instantiateWith();

        // then
        assertThat(result, not(nullValue()));
    }

    @Test
    public void shouldHaveEmptyDependenciesAndAnnotations() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.FallbackClass.class).get();

        // when
        Class<?>[] dependencies = instantiation.getDependencies();
        Annotation[][] annotations = instantiation.getDependencyAnnotations();

        // then
        assertThat(dependencies, emptyArray());
        assertThat(annotations, emptyArray());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowIfArgumentsAreSupplied() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.FallbackClass.class).get();

        // when / then
        instantiation.instantiateWith("some argument");
    }

    @Test
    public void shouldReturnNullForClassWithInjectMethod() {
        // given / when
        Instantiation<InstantiationFallbackClasses.InvalidInjectOnMethodClass> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.InvalidInjectOnMethodClass.class).get();

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForMissingNoArgsConstructor() {
        // given / when
        Instantiation<InstantiationFallbackClasses.InvalidFallbackClass> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.InvalidFallbackClass.class).get();

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForDifferentInjectionType() {
        // given / when
        Instantiation<GammaService> instantiation = InstantiationFallback.provide(GammaService.class).get();

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForClassWithPostConstruct() {
        // given / when
        Instantiation<InstantiationFallbackClasses.ClassWithPostConstruct> instantiation =
            InstantiationFallback.provide(InstantiationFallbackClasses.ClassWithPostConstruct.class).get();

        // then
        assertThat(instantiation, nullValue());
    }

}
