package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.Injector;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * Test for {@link PreConstructPackageValidator}.
 */
public class PreConstructPackageValidatorTest {

    private PreConstructPackageValidator validator =
        new PreConstructPackageValidator("ch.jalu.injector");

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Test
    public void shouldAcceptValidPackage() {
        // given / when
        validator.process(Injector.class);
        validator.process(PostConstructHandler.class);
        validator.process(DependencyHandler.class);

        // then - no exception thrown
    }

    @Test
    public void shouldThrowForPrimitiveClass() {
        // expect
        exceptionCatcher.expect("Primitive types must be provided");

        // when
        validator.process(boolean.class);
    }

    @Test
    public void shouldThrowForArrayClass() {
        // expect
        exceptionCatcher.expect("Unknown how to inject array classes");

        // when
        validator.process(PostConstructHandler[].class);
    }

    @Test
    public void shouldRejectClassWithInvalidPackage() {
        // expect
        exceptionCatcher.expect("outside of the allowed packages");

        // when
        validator.process(Test.class);
    }

}