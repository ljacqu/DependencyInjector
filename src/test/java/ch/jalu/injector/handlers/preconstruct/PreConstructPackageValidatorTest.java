package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.Injector;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.annotations.AllTypes;
import ch.jalu.injector.context.UnresolvedInstantiationContext;
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
        validator.accept(buildContext(Injector.class));
        validator.accept(buildContext(PostConstructHandler.class));
        validator.accept(buildContext(AllTypes.class));

        // then - no exception thrown
    }

    @Test
    public void shouldThrowForPrimitiveClass() {
        // expect
        exceptionCatcher.expect("Primitive types must be provided");

        // when
        validator.accept(buildContext(boolean.class));
    }

    @Test
    public void shouldThrowForArrayClass() {
        // expect
        exceptionCatcher.expect("Unknown how to inject array classes");

        // when
        validator.accept(buildContext(PostConstructHandler[].class));
    }

    @Test
    public void shouldRejectClassWithInvalidPackage() {
        // expect
        exceptionCatcher.expect("outside of the allowed packages");

        // when
        validator.accept(buildContext(Test.class));
    }

    private static <T> UnresolvedInstantiationContext<T> buildContext(Class<T> clz) {
        return new UnresolvedInstantiationContext<>(null, null, clz);
    }
}