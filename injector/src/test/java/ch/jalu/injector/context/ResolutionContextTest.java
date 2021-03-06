package ch.jalu.injector.context;

import ch.jalu.injector.exceptions.InjectorException;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test for {@link ResolutionContext}.
 */
class ResolutionContextTest {

    @Test
    void shouldThrowForMappedClassThatIsNotChild() {
        // given
        ResolutionContext context =
            new ResolutionContext(null, new ObjectIdentifier(StandardResolutionType.SINGLETON, Number.class));

        // when
        try {
            context.setIdentifier(new ObjectIdentifier(StandardResolutionType.SINGLETON, String.class));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            // then
            assertThat(e.getMessage(),
                containsString("New mapped class '" + String.class + "' is not a child of original class"));
        }
    }
}