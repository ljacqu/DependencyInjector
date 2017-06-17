package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link UnresolvedContext}.
 */
public class UnresolvedContextTest {

    @Test
    public void shouldThrowForMappedClassThatIsNotChild() {
        // given
        UnresolvedContext context =
            new UnresolvedContext(null, null, new ObjectIdentifier(Number.class));

        // when
        try {
            context.setIdentifier(new ObjectIdentifier(String.class));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // then
            assertThat(e.getMessage(),
                containsString("New mapped class '" + String.class + "' is not a child of original class"));
        }
    }

    @Test
    public void shouldCreateResolvedContext() {
        // given
        Injector injector = mock(Injector.class);
        ResolutionType resolutionType = StandardResolutionType.REQUEST_SCOPED;
        UnresolvedContext context = new UnresolvedContext(injector, resolutionType, new ObjectIdentifier(Number.class));
        context.setIdentifier(new ObjectIdentifier(Double.class));
        Instantiation instantiation = mock(Instantiation.class);

        // when
        ResolvedContext resolvedContext = context.buildResolvedContext(instantiation);

        // then
        assertThat(resolvedContext.getInstantiation(), equalTo(instantiation));
        assertThat(resolvedContext.getInjector(), equalTo(injector));
        assertThat(resolvedContext.getResolutionType(), equalTo(resolutionType));
        assertThat(resolvedContext.getOriginalIdentifier().getType(), equalTo(Number.class));
        assertThat(resolvedContext.getIdentifier().getType(), equalTo(Double.class));
    }
}