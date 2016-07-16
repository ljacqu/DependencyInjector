package ch.jalu.injector.utils;

import ch.jalu.injector.TestUtils;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Method;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link InjectorUtils}.
 */
public class InjectorUtilsTest {

    private static final String DEFAULT_NOT_NULL_MSG = "Object may not be null";

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Test
    public void shouldPassSimpleChecks() {
        InjectorUtils.checkNotNull(new Object());
        InjectorUtils.checkNotNull(new Object(), "New Object should not be null");

        String[] arr = {"this", "is", "a", "test", "array"};
        InjectorUtils.checkArgument(arr.length == 5, "Arr length should be 5");
        InjectorUtils.checkNoNullValues(arr);
    }

    @Test
    public void shouldThrowWithCustomMessage() {
        // given
        String message = "custom msg";
        Object o = null;

        // expect
        exceptionCatcher.expect(message);

        // when
        InjectorUtils.checkNotNull(o, message);
    }

    @Test
    public void shouldThrowForArrayWithNullValue() {
        // given
        String[] elems = {"this", "is", null, "test", "array"};

        // expect
        exceptionCatcher.expect(DEFAULT_NOT_NULL_MSG);

        // when
        InjectorUtils.checkNoNullValues(elems);
    }

    @Test
    public void shouldThrowForNullIterable() {
        // given
        Iterable<Boolean> elems = null;

        // expect
        exceptionCatcher.expect(DEFAULT_NOT_NULL_MSG);

        // when
        InjectorUtils.checkNoNullValues(elems);
    }

    @Test
    public void shouldThrowForNullArray() {
        // given
        String[] arr = null;

        // expect
        exceptionCatcher.expect(DEFAULT_NOT_NULL_MSG);

        // when
        InjectorUtils.checkNotNull(arr);
    }

    @Test
    public void shouldThrowForInvalidArgumentCheck() {
        // given
        String msg = "Argument check was unsuccessful";

        // expect
        exceptionCatcher.expect(msg);

        // when
        InjectorUtils.checkArgument(false, msg);
    }

    @Test
    public void shouldRethrowException() {
        // given
        Exception e = new IllegalArgumentException("Original exception is this");

        // when / then
        try {
            InjectorUtils.rethrowException(e);
            fail("Expected exception to have been thrown");
        } catch (InjectorException ex) {
            assertThat(ex.getMessage(), containsString("An error occurred"));
            assertThat(ex.getCause(), Matchers.<Throwable>equalTo(e));
        }
    }

    @Test
    public void shouldForwardException() {
        // given
        InjectorException e = new InjectorException("Error during injection");

        // when / then
        try {
            InjectorUtils.rethrowException(e);
            fail("Expected exception to have been thrown");
        } catch (InjectorException ex) {
            assertThat(ex, equalTo(e));
            assertThat(ex.getCause(), nullValue());
        }
    }

    @Test
    public void shouldHandleNullExceptionProperly() {
        // given
        InjectorException e = null;

        // when / then
        try {
            InjectorUtils.rethrowException(e);
            fail("Expected exception to have been thrown");
        } catch (InjectorException ex) {
            assertThat(ex.getMessage(), containsString("An error occurred"));
            assertThat(ex.getCause(), nullValue());
        }
    }

    @Test
    public void shouldReturnFirstNotNull() {
        // given
        String[] arr = {null, null, "test", null, "other"};

        // when
        String result = InjectorUtils.firstNotNull(arr);

        // then
        assertThat(result, equalTo("test"));
    }

    @Test
    public void shouldReturnNull() {
        Object[] arr = {null, null, null};
        assertThat(InjectorUtils.firstNotNull(arr), nullValue());

        Integer[] empty = {};
        assertThat(InjectorUtils.firstNotNull(empty), nullValue());
    }

    @Test
    public void shouldEvaluateIfInstantiable() {
        // interface
        assertThat(InjectorUtils.canInstantiate(Iterable.class), equalTo(false));
        // enum
        assertThat(InjectorUtils.canInstantiate(SampleEnum.class), equalTo(false));
        // abstract
        assertThat(InjectorUtils.canInstantiate(TypeSafeAnnotationHandler.class), equalTo(false));
        // instantiable:
        assertThat(InjectorUtils.canInstantiate(InjectorUtils.class), equalTo(true));
    }

    @Test
    public void shouldReturnDeclaringClass() throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod("shouldReturnDeclaringClass");
        assertThat(InjectorUtils.getDeclarer(method), equalTo(getClass().getName()));

        assertThat(InjectorUtils.getDeclarer(null), equalTo("null"));
    }

    @Test
    public void shouldBeWellFormedUtilsClass() {
        TestUtils.assertIsProperUtilsClass(InjectorUtils.class);
    }

    private enum SampleEnum {

    }

}