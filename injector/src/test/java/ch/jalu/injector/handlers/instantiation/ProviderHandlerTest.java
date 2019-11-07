package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.providers.Alfa;
import ch.jalu.injector.handlers.dependency.providers.Bravo;
import ch.jalu.injector.handlers.dependency.providers.Charlie;
import ch.jalu.injector.handlers.dependency.providers.ClassWithInjectedProviders;
import ch.jalu.injector.handlers.dependency.providers.Delta;
import ch.jalu.injector.handlers.dependency.providers.Delta1;
import ch.jalu.injector.handlers.dependency.providers.Delta1Provider;
import ch.jalu.injector.handlers.dependency.providers.Delta2;
import ch.jalu.injector.handlers.dependency.providers.Delta2Provider;
import org.junit.jupiter.api.Test;

import javax.inject.Provider;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link ProviderHandler}.
 */
class ProviderHandlerTest {

    private ProviderHandler providerHandler = new ProviderHandler();

    @Test
    void shouldThrowForAlreadyRegisteredProvider() {
        // given
        Charlie charlie = mock(Charlie.class);
        providerHandler.onProvider(Delta.class, new Delta2Provider(charlie));

        // when / then
        try {
            providerHandler.onProvider(Delta.class, new Delta1Provider());
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // all good
        }
    }

    @Test
    void shouldThrowForAlreadyRegisteredProvider2() {
        // given
        Charlie charlie = mock(Charlie.class);
        providerHandler.onProvider(Delta.class, new Delta2Provider(charlie));

        // when / then
        try {
            providerHandler.onProviderClass(Delta.class, Delta2Provider.class);
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // all good
        }
    }

    @Test
    void shouldReturnNullForClassWithoutProvider() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);

        // when
        Resolution<?> result = providerHandler.resolve(newContext(Alfa.class));

        // then
        assertThat(result, nullValue());
    }

    @Test
    void shouldReturnWrappedProviderAsInstantiation() {
        // given
        Charlie charlie = mock(Charlie.class);
        providerHandler.onProvider(Delta.class, new Delta2Provider(charlie));

        // when
        Resolution<?> instantiation = providerHandler.resolve(newContext(Delta.class));

        // then
        assertThat(instantiation, not(nullValue()));
        assertThat(instantiation.getDependencies(), empty());
    }

    @Test
    void shouldReturnProviderClassWrappedAsInstantiation() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);

        // when
        Resolution<?> instantiation = providerHandler.resolve(newContext(Delta.class));

        // then
        assertThat(instantiation, not(nullValue()));
        assertThat(instantiation.getDependencies(), hasSize(1));
        assertThat(instantiation.getDependencies().get(0).getType(), equalTo(Delta2Provider.class));

        // given (2)
        Charlie charlie = mock(Charlie.class);
        Delta2Provider provider = new Delta2Provider(charlie);

        // when (2)
        Delta delta = (Delta) instantiation.instantiateWith(provider);

        // then (2)
        assertThat(delta, instanceOf(Delta2.class));
    }

    @Test
    void shouldThrowForInvalidArgument() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);
        Resolution<?> instantiation = providerHandler.resolve(newContext(Delta.class));

        // when / then
        try {
            instantiation.instantiateWith(mock(Charlie.class));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // all good
        }
    }

    @Test
    void shouldInstantiateClassWithProvider() {
        // given
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers("ch.jalu.injector")
            .create();
        Charlie charlie = injector.getSingleton(Charlie.class);
        injector.registerProvider(Delta.class, new Delta2Provider(charlie));

        // when
        Delta delta = injector.getSingleton(Delta.class);

        // then
        assertThat(delta, instanceOf(Delta2.class));
        assertThat(delta.getName(), equalTo("pre_BRAVO_CHARLIE"));
    }

    @Test
    void shouldInstantiateClassWithProviderClass() {
        // given
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers("ch.jalu.injector")
            .create();
        injector.registerProvider(Delta.class, Delta1Provider.class);

        // when
        Delta delta = injector.getSingleton(Delta.class);

        // then
        assertThat(delta, instanceOf(Delta1.class));
        assertThat(delta.getName(), equalTo("BRAVO19"));
    }

    @Test
    void shouldInjectProviderAndCustomProvider() {
        // given
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers("ch.jalu.injector")
            .create();
        Charlie charlie = mock(Charlie.class);
        injector.register(Charlie.class, charlie);
        Delta2Provider delta2Provider = new Delta2Provider(charlie);
        injector.registerProvider(Delta.class, delta2Provider);

        // when
        ClassWithInjectedProviders cwip = injector.getSingleton(ClassWithInjectedProviders.class);

        // then
        assertThat(cwip.getDeltaProvider(), sameInstance(delta2Provider));
        List<Charlie> charlies = cwip.charlieList();
        assertThat(charlies, hasSize(3));
        assertThat(charlies, contains(not(nullValue()), not(nullValue()), not(nullValue())));
        assertThat(cwip.getBravo(), sameInstance(injector.getSingleton(Bravo.class)));
    }

    @Test
    void shouldInjectProviderAndCustomProviderClass() {
        // given
        Injector injector = new InjectorBuilder()
                .addDefaultHandlers("ch.jalu.injector")
                .create();
        Bravo bravo = mock(Bravo.class);
        injector.register(Bravo.class, bravo);
        injector.registerProvider(Delta.class, Delta1Provider.class);

        // when
        ClassWithInjectedProviders cwip = injector.getSingleton(ClassWithInjectedProviders.class);

        // then
        assertThat(cwip.getDeltaProvider(), sameInstance(injector.getSingleton(Delta1Provider.class)));
        List<Charlie> charlies = cwip.charlieList();
        assertThat(charlies, hasSize(3));
        assertThat(charlies, contains(not(nullValue()), not(nullValue()), not(nullValue())));
        assertThat(cwip.getBravo(), sameInstance(bravo));
    }

    @Test
    void shouldThrowForMissingGenericInfo() {
        // given
        ProviderHandler providerHandler = new ProviderHandler();
        Injector injector = mock(Injector.class);
        ResolutionContext context = new ResolutionContext(
            injector, new ObjectIdentifier(null, Provider.class));

        // when / then
        try {
            providerHandler.resolve(context);
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            assertThat(e.getMessage(), equalTo("Injection of a provider was requested but no generic type was given"));
            verifyZeroInteractions(injector);
        }
    }

    @Test
    void shouldIgnoreNonProviderDependency() {
        // given
        ProviderHandler providerHandler = new ProviderHandler();
        Injector injector = mock(Injector.class);
        ResolutionContext context = new ResolutionContext(
            injector, new ObjectIdentifier(null, Bravo.class));

        // when
        Resolution<?> value = providerHandler.resolve(context);

        // then
        assertThat(value, nullValue());
        verifyZeroInteractions(injector);
    }

    private static ResolutionContext newContext(Class<?> clz) {
        return new ResolutionContext(null, new ObjectIdentifier(null, clz));
    }
}
