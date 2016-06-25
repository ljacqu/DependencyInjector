package ch.jalu.injector.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationFallback;
import ch.jalu.injector.handlers.instantiation.InstantiationFallbackProvider;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InstantiationFallbackClasses;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link InstantiationFallback}.
 */
public class InstantiationFallbackTest {

    private InstantiationFallbackProvider provider = new InstantiationFallbackProvider();

    @Test
    public void shouldInstantiateClass() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            provider.get(InstantiationFallbackClasses.FallbackClass.class);

        // when
        InstantiationFallbackClasses.FallbackClass result = instantiation.instantiateWith();

        // then
        assertThat(result, not(nullValue()));
    }

    @Test
    public void shouldHaveEmptyDependenciesAndAnnotations() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            provider.get(InstantiationFallbackClasses.FallbackClass.class);

        // when
        List<DependencyDescription> dependencies = instantiation.getDependencies();

        // then
        assertThat(dependencies, empty());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowIfArgumentsAreSupplied() {
        // given
        Instantiation<InstantiationFallbackClasses.FallbackClass> instantiation =
            provider.get(InstantiationFallbackClasses.FallbackClass.class);

        // when / then
        instantiation.instantiateWith("some argument");
    }

    @Test
    public void shouldReturnNullForClassWithInjectMethod() {
        // given / when
        Instantiation<InstantiationFallbackClasses.InvalidInjectOnMethodClass> instantiation =
            provider.get(InstantiationFallbackClasses.InvalidInjectOnMethodClass.class);

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForMissingNoArgsConstructor() {
        // given / when
        Instantiation<InstantiationFallbackClasses.InvalidFallbackClass> instantiation =
            provider.get(InstantiationFallbackClasses.InvalidFallbackClass.class);

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForDifferentInjectionType() {
        // given / when
        Instantiation<GammaService> instantiation = provider.get(GammaService.class);

        // then
        assertThat(instantiation, nullValue());
    }

    @Test
    public void shouldReturnNullForClassWithPostConstruct() {
        // given / when
        Instantiation<InstantiationFallbackClasses.ClassWithPostConstruct> instantiation =
            provider.get(InstantiationFallbackClasses.ClassWithPostConstruct.class);

        // then
        assertThat(instantiation, nullValue());
    }

}
