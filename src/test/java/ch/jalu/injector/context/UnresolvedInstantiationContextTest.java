package ch.jalu.injector.context;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import org.junit.Test;

import static ch.jalu.injector.TestUtils.isClass;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

/**
 * Test for {@link UnresolvedInstantiationContext}.
 */
public class UnresolvedInstantiationContextTest {

    @Test
    public void shouldThrowForMappedClassThatIsNotChild() {
        // given
        UnresolvedInstantiationContext<Number> context =
            new UnresolvedInstantiationContext<>(null, null, Number.class);

        // when
        try {
            context.setMappedClass((Class) String.class);
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
        UnresolvedInstantiationContext<Number> context =
            new UnresolvedInstantiationContext<>(injector, resolutionType, Number.class);
        context.setMappedClass(Double.class);
        Instantiation instantiation = mock(Instantiation.class);

        // when
        ResolvedInstantiationContext<Number> resolvedContext = context.buildResolvedContext(instantiation);

        // then
        assertThat(resolvedContext.getInstantiation(), equalTo(instantiation));
        assertThat(resolvedContext.getInjector(), equalTo(injector));
        assertThat(resolvedContext.getResolutionType(), equalTo(resolutionType));
        assertThat(resolvedContext.getOriginalClass(), isClass(Number.class));
        assertThat(resolvedContext.getMappedClass(), isClass(Double.class));
    }
}