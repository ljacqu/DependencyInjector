package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.Injector;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.context.UnresolvedInstantiationContext;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.provider.ProviderHandlerImpl;
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
        validator.preProcess(buildContext(Injector.class));
        validator.preProcess(buildContext(PostConstructMethodInvoker.class));
        validator.preProcess(buildContext(NoFieldScan.class));

        // then - no exception thrown
    }

    @Test
    public void shouldThrowForPrimitiveClass() {
        // expect
        exceptionCatcher.expect("Primitive types must be provided");

        // when
        validator.preProcess(buildContext(boolean.class));
    }

    @Test
    public void shouldThrowForArrayClass() {
        // expect
        exceptionCatcher.expect("Unknown how to inject array classes");

        // when
        validator.preProcess(buildContext(ProviderHandlerImpl[].class));
    }

    @Test
    public void shouldRejectClassWithInvalidPackage() {
        // expect
        exceptionCatcher.expect("outside of the allowed packages");

        // when
        validator.preProcess(buildContext(Test.class));
    }

    private static <T> UnresolvedInstantiationContext<T> buildContext(Class<T> clz) {
        return new UnresolvedInstantiationContext<>(null, null, clz);
    }
}