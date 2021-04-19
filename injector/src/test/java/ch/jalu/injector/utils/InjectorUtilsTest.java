package ch.jalu.injector.utils;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.TypeSafeAnnotationHandler;
import org.junit.jupiter.api.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link InjectorUtils}.
 */
class InjectorUtilsTest {

    private static final String DEFAULT_NOT_NULL_MSG = "Object may not be null";

    @Test
    void shouldPassSimpleChecks() {
        InjectorUtils.checkNotNull(new Object());
        InjectorUtils.checkNotNull(new Object(), "New Object should not be null");

        String[] arr = {"this", "is", "a", "test", "array"};
        InjectorUtils.checkArgument(arr.length == 5, "Arr length should be 5");
        InjectorUtils.checkNoNullValues(arr);
    }

    @Test
    void shouldThrowWithCustomMessage() {
        // given
        String message = "custom msg";
        Object o = null;

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.checkNotNull(o, message));

        // then
        assertThat(ex.getMessage(), equalTo(message));
    }

    @Test
    void shouldThrowForArrayWithNullValue() {
        // given
        String[] elems = {"this", "is", null, "test", "array"};

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.checkNoNullValues(elems));

        // then
        assertThat(ex.getMessage(), equalTo(DEFAULT_NOT_NULL_MSG));
    }

    @Test
    void shouldThrowForNullIterable() {
        // given
        Iterable<Boolean> elems = null;

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.checkNoNullValues(elems));

        // then
        assertThat(ex.getMessage(), equalTo(DEFAULT_NOT_NULL_MSG));
    }

    @Test
    void shouldThrowForNullArray() {
        // given
        String[] arr = null;

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.checkNotNull(arr));

        // then
        assertThat(ex.getMessage(), equalTo(DEFAULT_NOT_NULL_MSG));
    }

    @Test
    void shouldThrowForInvalidArgumentCheck() {
        // given
        String msg = "Argument check was unsuccessful";

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.checkArgument(false, msg));

        // then
        assertThat(ex.getMessage(), equalTo(msg));
    }

    @Test
    void shouldRethrowException() {
        // given
        Exception e = new IllegalArgumentException("Original exception is this");

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.rethrowException(e));

        // then
        assertThat(ex.getMessage(), containsString("An error occurred"));
        assertThat(ex.getCause(), equalTo(e));
    }

    @Test
    void shouldForwardException() {
        // given
        InjectorException e = new InjectorException("Error during injection");

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.rethrowException(e));

        // then
        assertThat(ex, equalTo(e));
        assertThat(ex.getCause(), nullValue());
    }

    @Test
    void shouldHandleNullExceptionProperly() {
        // given
        InjectorException e = null;

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> InjectorUtils.rethrowException(e));

        // then
        assertThat(ex.getMessage(), containsString("An error occurred"));
        assertThat(ex.getCause(), nullValue());
    }

    @Test
    void shouldReturnFirstNotNull() {
        assertThat(InjectorUtils.firstNotNull(null, "test"), equalTo("test"));
        assertThat(InjectorUtils.firstNotNull("rest", "pest"), equalTo("rest"));
        assertThat(InjectorUtils.firstNotNull(null, null), nullValue());
    }

    @Test
    void shouldEvaluateIfInstantiable() {
        // interface
        assertThat(InjectorUtils.canInstantiate(Iterable.class), equalTo(false));
        // enum
        assertThat(InjectorUtils.canInstantiate(SampleEnum.class), equalTo(false));
        // abstract
        assertThat(InjectorUtils.canInstantiate(TypeSafeAnnotationHandler.class), equalTo(false));
        // array
        assertThat(InjectorUtils.canInstantiate(Object[].class), equalTo(false));
        // instantiable:
        assertThat(InjectorUtils.canInstantiate(InjectorUtils.class), equalTo(true));
    }

    @Test
    void shouldFindNullInArray() {
        // given
        String[] arr1 = {"a", "b", "c", "d", "e"};
        Double[] arr2 = {2.0, 3.1, 4.4, null, 6.7};
        Character[] arr3 = {null, 'a', 'b', 'c'};

        // when
        boolean result1 = InjectorUtils.containsNullValue(arr1);
        boolean result2 = InjectorUtils.containsNullValue(arr2);
        boolean result3 = InjectorUtils.containsNullValue(arr3);

        // then
        assertThat(result1, equalTo(false));
        assertThat(result2, equalTo(true));
        assertThat(result3, equalTo(true));
    }

    private enum SampleEnum {

    }
}