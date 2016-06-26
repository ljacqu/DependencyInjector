package ch.jalu.injector.utils;

import ch.jalu.injector.TestUtils;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link InjectorUtils}.
 */
public class InjectorUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Test
    public void shouldPassSimpleChecks() {
        String[] arr = {"this", "is", "a", "test", "array"};
        List<Integer> intList = Arrays.asList(1, 2, 3, 4);

        // Tests all flavors of checkNotNull() with non-null arguments
        InjectorUtils.checkNotNull(new Object(), Object.class);
        InjectorUtils.checkNotNull(new Object(), "New Object should not be null", Random.class);
        InjectorUtils.checkNotNull(intList, Integer.class);
        InjectorUtils.checkNotNull(arr, String.class);

        InjectorUtils.checkArgument(arr.length == 5, "Arr length should be 5", String.class);
    }

    @Test
    public void shouldThrowWithCustomMessage() {
        // given
        String message = "custom msg";
        Object o = null;

        // expect
        exceptionCatcher.expect(message, Object.class);

        // when
        InjectorUtils.checkNotNull(o, message, Object.class);
    }

    @Test
    public void shouldThrowForIterableWithNullValue() {
        // given
        Iterable<String> elems = Arrays.asList("this", "is", null, "test", "array");

        // expect
        exceptionCatcher.expect("Object may not be null", String.class);

        // when
        InjectorUtils.checkNotNull(elems, String.class);
    }

    @Test
    public void shouldThrowForNullIterable() {
        // given
        Iterable<Boolean> elems = null;

        // expect
        exceptionCatcher.expect("Object may not be null", Iterable.class);

        // when
        InjectorUtils.checkNotNull(elems, Iterable.class);
    }

    @Test
    public void shouldThrowForArrayWithNullValue() {
        // given
        Object[] arr = {new Object(), new Object(), null, "test"};

        // expect
        exceptionCatcher.expect("Object may not be null", Class.class);

        // when
        InjectorUtils.checkNotNull(arr, Class.class);
    }

    @Test
    public void shouldThrowForNullArray() {
        // given
        String[] arr = null;

        // expect
        exceptionCatcher.expect("Object may not be null", Random.class);

        // when
        InjectorUtils.checkNotNull(arr, Random.class);
    }

    @Test
    public void shouldThrowForInvalidArgumentCheck() {
        // given
        String msg = "Argument check was unsuccessful";

        // expect
        exceptionCatcher.expect(msg, Object.class);

        // when
        InjectorUtils.checkArgument(false, msg, Object.class);
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
        InjectorException e = new InjectorException("Error during injection", List.class);

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
    public void shouldBeWellFormedUtilsClass() {
        TestUtils.assertIsProperUtilsClass(InjectorUtils.class);
    }

    private enum SampleEnum {

    }

}