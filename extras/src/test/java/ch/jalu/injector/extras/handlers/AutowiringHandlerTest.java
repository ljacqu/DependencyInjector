package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.extras.samples.animals.ISparrow;
import ch.jalu.injector.extras.samples.animals.Lion;
import ch.jalu.injector.extras.samples.animals.Mammal;
import ch.jalu.injector.extras.samples.animals.Sparrow;
import ch.jalu.injector.extras.samples.animals.services.BasicService;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link AutowiringHandler}.
 */
public class AutowiringHandlerTest {

    @Test
    public void shouldFindUniqueImplementation() {
        // given
        String pkg = "ch.jalu.injector.extra";
        Injector injector = new InjectorBuilder()
                .addHandlers(new AutowiringHandler(pkg))
                .addDefaultHandlers(pkg)
                .create();

        // when
        ISparrow sparrow = injector.getSingleton(ISparrow.class);
        Mammal mammal = injector.getSingleton(Mammal.class);

        // then
        assertThat(sparrow, instanceOf(Sparrow.class));
        assertThat(mammal, instanceOf(Lion.class));
    }

    @Test
    public void shouldReturnNullForZeroOrMultipleImplementations() throws Exception {
        // given
        String pkg = "ch.jalu.injector.extra";
        AutowiringHandler autowiringHandler = new AutowiringHandler(pkg);

        // when
        Class<?> noImpl = autowiringHandler.accept(NoImpl.class);
        Class<?> noImpl2 = autowiringHandler.accept(AbstrNoImpl.class);
        Class<?> basicService = autowiringHandler.accept(BasicService.class);

        // then
        assertThat(noImpl, nullValue());
        assertThat(noImpl2, nullValue());
        assertThat(basicService, nullValue());
    }

    @Test
    public void shouldThrowForMultipleImplementations() {
        // given
        String pkg = "ch.jalu.injector.extra";
        AutowiringHandler autowiringHandler = new AutowiringHandler(pkg);
        autowiringHandler.setThrowIfNoUniqueSubtypeFound(true);

        Injector injector = new InjectorBuilder()
                .addHandlers(autowiringHandler)
                .addDefaultHandlers(pkg)
                .create();

        // when / then
        try {
            injector.getSingleton(BasicService.class);
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            assertThat(e.getMessage(), containsString("Found multiple subtypes"));
        }
    }

    @Test
    public void shouldThrowForNoImplementation() {
        // given
        String pkg = "ch.jalu.injector.extra";
        AutowiringHandler autowiringHandler = new AutowiringHandler(pkg);
        autowiringHandler.setThrowIfNoUniqueSubtypeFound(true);

        Injector injector = new InjectorBuilder()
                .addHandlers(autowiringHandler)
                .addDefaultHandlers(pkg)
                .create();

        // when / then
        try {
            injector.getSingleton(NoImpl.class);
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            assertThat(e.getMessage(), containsString("Did not find any subtype"));
        }
    }

    private interface NoImpl {
    }

    private abstract class AbstrNoImpl implements NoImpl {
    }

}