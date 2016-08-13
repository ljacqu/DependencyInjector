package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.provider.impl.Alfa;
import ch.jalu.injector.handlers.provider.impl.Charlie;
import ch.jalu.injector.handlers.provider.impl.Delta;
import ch.jalu.injector.handlers.provider.impl.Delta1;
import ch.jalu.injector.handlers.provider.impl.Delta1Provider;
import ch.jalu.injector.handlers.provider.impl.Delta2;
import ch.jalu.injector.handlers.provider.impl.Delta2Provider;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link ProviderHandlerImpl}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProviderHandlerImplTest {

    private ProviderHandlerImpl providerHandler = new ProviderHandlerImpl();

    @Test
    public void shouldThrowForAlreadyRegisteredProvider() {
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
    public void shouldThrowForAlreadyRegisteredProvider2() {
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
    public void shouldReturnNullForClassWithoutProvider() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);

        // when
        Instantiation<Alfa> result = providerHandler.get(Alfa.class);

        // then
        assertThat(result, nullValue());
    }

    @Test
    public void shouldReturnWrappedProviderAsInstantiation() {
        // given
        Charlie charlie = mock(Charlie.class);
        providerHandler.onProvider(Delta.class, new Delta2Provider(charlie));

        // when
        Instantiation<Delta> instantiation = providerHandler.get(Delta.class);

        // then
        assertThat(instantiation, not(nullValue()));
        assertThat(instantiation.getDependencies(), empty());
    }

    @Test
    public void shouldReturnProviderClassWrappedAsInstantiation() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);

        // when
        Instantiation<Delta> instantiation = providerHandler.get(Delta.class);

        // then
        assertThat(instantiation, not(nullValue()));
        assertThat(instantiation.getDependencies(), hasSize(1));
        assertThat(instantiation.getDependencies().get(0).getType(), Matchers.<Class<?>>equalTo(Delta2Provider.class));

        // given (2)
        Charlie charlie = mock(Charlie.class);
        Delta2Provider provider = new Delta2Provider(charlie);

        // when (2)
        Delta delta = instantiation.instantiateWith(provider);

        // then (2)
        assertThat(delta, instanceOf(Delta2.class));
        assertThat(providerHandler.get(Delta.class).getDependencies(), empty());
    }

    @Test
    public void shouldThrowForInvalidArgument() {
        // given
        providerHandler.onProviderClass(Delta.class, Delta2Provider.class);
        Instantiation<Delta> instantiation = providerHandler.get(Delta.class);

        // when / then
        try {
            instantiation.instantiateWith(mock(Charlie.class));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // all good
        }
    }

    @Test
    // TODO #27: Change test to use interface instead of impl. class
    public void shouldInstantiateClassWithProvider() {
        // given
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers("ch.jalu.injector")
            .create();
        Charlie charlie = injector.getSingleton(Charlie.class);
        injector.registerProvider(Delta2.class, new Delta2Provider(charlie));

        // when
        Delta delta = injector.getSingleton(Delta2.class);

        // then
        assertThat(delta, instanceOf(Delta2.class));
        assertThat(delta.getName(), equalTo("pre_BRAVO_CHARLIE"));
    }

    @Test
    // TODO #27: Change test to use interface instead of impl. class
    public void shouldInstantiateClassWithProviderClass() {
        // given
        Injector injector = new InjectorBuilder()
                .addDefaultHandlers("ch.jalu.injector")
                .create();
        injector.registerProvider(Delta1.class, Delta1Provider.class);

        // when
        Delta delta = injector.getSingleton(Delta1.class);

        // then
        assertThat(delta, instanceOf(Delta1.class));
        assertThat(delta.getName(), equalTo("BRAVO19"));
    }

}
