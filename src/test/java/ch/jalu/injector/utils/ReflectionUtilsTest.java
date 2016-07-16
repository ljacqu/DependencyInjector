package ch.jalu.injector.utils;

import ch.jalu.injector.TestUtils;
import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.annotations.NoMethodScan;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.exceptions.InjectorReflectionException;
import org.hamcrest.Matcher;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Test for {@link ReflectionUtils}.
 */
public class ReflectionUtilsTest {

    // Used as sample Field in test
    private Object field;
    // Used as sample Field in test
    private List<Boolean> boolList;

    @Test
    public void shouldGetFieldValue() {
        // given
        String str = "string value sample";
        ReflectionsTestClass testClass = new ReflectionsTestClass(str, 123);
        Field field = getField("string");

        // when
        Object result = ReflectionUtils.getFieldValue(field, testClass);

        // then
        assertThat(result, equalTo((Object) str));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldForwardException() throws NoSuchFieldException {
        // given
        ReflectionsTestClass testClass = new ReflectionsTestClass("abc", 123);
        Field field = ReflectionUtilsTest.class.getDeclaredField("field");

        // when
        ReflectionUtils.getFieldValue(field, testClass);
    }

    @Test
    public void shouldGetStaticFieldValue() throws NoSuchFieldException {
        // given
        Object o = new Object();
        ReflectionsTestClass.changeStaticObject(o);
        Field field = getField("staticObject");

        // when
        Object result = ReflectionUtils.getFieldValue(field, null);

        // then
        assertThat(result, equalTo(o));
    }

    @Test
    public void shouldSetField() {
        // given
        ReflectionsTestClass testClass = new ReflectionsTestClass("abc", 123);
        Field field = getField("integer");

        // when
        ReflectionUtils.setField(field, testClass, 444);

        // then
        assertThat(testClass.getInteger(), equalTo(444));
    }

    @Test
    public void shouldSetStaticField() {
        // given
        Field field = getField("staticObject");
        Object o = new Object();

        // when
        ReflectionUtils.setField(field, null, o);

        // then
        assertThat(ReflectionsTestClass.changeStaticObject(null), equalTo(o));
    }

    @Test
    public void shouldInvokeMethod() {
        // given
        ReflectionsTestClass testClass = new ReflectionsTestClass("", 123);
        Method method = getMethod("setIntegerField", Integer.class);

        // when
        ReflectionUtils.invokeMethod(method, testClass, 144);

        // then
        assertThat(testClass.getInteger(), equalTo(144));
    }

    @Test
    public void shouldInvokeStaticMethod() {
        // given
        Object oldValue = new Object();
        ReflectionsTestClass.changeStaticObject(oldValue);
        Method method = getMethod("changeStaticObject", Object.class);
        Object o = new Object();

        // when
        Object result = ReflectionUtils.invokeMethod(method, null, o);

        // then
        assertThat(result, equalTo(oldValue));
        assertThat(ReflectionsTestClass.changeStaticObject(null), equalTo(o));
    }

    @Test(expected = InjectorReflectionException.class)
    public void shouldForwardExceptionFromMethod() {
        // given
        ReflectionsTestClass testClass = new ReflectionsTestClass("", 123);
        Method method = getMethod("throwingMethod");

        // when
        ReflectionUtils.invokeMethod(method, testClass);
    }

    @Test
    public void shouldCreateNewInstance() throws NoSuchMethodException {
        // given
        Constructor<ReflectionsTestClass> constr = ReflectionsTestClass.class
            .getDeclaredConstructor(String.class, int.class);

        // when
        ReflectionsTestClass testClass = ReflectionUtils.newInstance(constr, "Test", 543);

        // then
        assertThat(testClass.getInteger(), equalTo(543));
    }

    @Test(expected = InjectorReflectionException.class)
    public void shouldForwardExceptionFromConstructor() throws NoSuchMethodException {
        // given
        Constructor<ReflectionsTestClass> constr = ReflectionsTestClass.class
            .getDeclaredConstructor(boolean.class);

        // when
        ReflectionUtils.newInstance(constr, true);
    }

    @Test
    public void shouldGetClassOfArray() {
        // given
        Class<?> mainType = Exception[].class;

        // when
        Class<?> result = ReflectionUtils.getGenericClass(mainType, null);

        // then
        assertThat(result, isClass(Exception.class));
    }

    @Test
    public void shouldReturnNullForNonGenericClass() {
        // given
        Class<?> mainType = Exception.class;
        Type genericType = mock(ParameterizedType.class);

        // when
        Class<?> result = ReflectionUtils.getGenericClass(mainType, genericType);

        // then
        assertThat(result, nullValue());
        verifyZeroInteractions(genericType);
    }

    @Test
    public void shouldReturnGenericTypeOfList() throws NoSuchFieldException {
        // given
        Field boolList = getClass().getDeclaredField("boolList");

        // when
        Class<?> result = ReflectionUtils.getGenericClass(boolList.getType(), boolList.getGenericType());

        // then
        assertThat(result, isClass(Boolean.class));
    }

    @Test
    public void shouldNotGetGenericTypeIfNotIterableSubtype() throws NoSuchFieldException {
        // given
        // take genericType from the List<Boolean> field again, avoids us having to mock
        Field boolList = getClass().getDeclaredField("boolList");

        // when
        Class<?> result = ReflectionUtils.getGenericClass(Class.class, boolList.getGenericType());

        // then
        assertThat(result, nullValue());
    }

    @Test
    public void shouldReturnArray() {
        // given
        Set<String> set = new HashSet<>(Arrays.asList("This", "is", "a", "test", "set", "wooo"));
        Class<?> rawType = String[].class;

        // when
        Object result = ReflectionUtils.toSuitableCollectionType(rawType, set);

        // then
        assertThat(result, instanceOf(rawType));
        String[] typedResult = (String[]) result;
        assertThat(typedResult, arrayContaining(set.toArray()));
    }

    @Test
    public void shouldReturnSameList() {
        // given
        Set<Integer> set = new HashSet<>(Arrays.asList(123, 456, 789, 12, 3, 45, 66, 78));
        Class<?> rawType = Set.class;

        // when
        Object result = ReflectionUtils.toSuitableCollectionType(rawType, set);

        // then
        assertThat(result == set, equalTo(true));
    }

    @Test
    public void shouldReturnList() {
        // given
        Set<Double> set = new HashSet<>(Arrays.asList(123.0, 11.2, 40.30, -198.432));
        Class<?> rawType = List.class;

        // when
        Object result = ReflectionUtils.toSuitableCollectionType(rawType, set);

        // then
        assertThat(result, instanceOf(rawType));
        List<Double> typedResult = (List<Double>) result;
        assertThat(typedResult, contains(set.toArray()));
    }

    @Test
    public void shouldReturnSetForSupertype() {
        // given
        Set<Integer> set = new HashSet<>(Arrays.asList(123, 456, 789, 12, 3, 45, 66, 78));
        Class<?> rawType = Collection.class;

        // when
        Object result = ReflectionUtils.toSuitableCollectionType(rawType, set);

        // then
        assertThat(result == set, equalTo(true));
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForUnmatchedType() {
        // given
        Set<Character> set = new HashSet<>(Arrays.asList('a', 'b', 'c', 'd'));
        Class<?> rawType = Map.class;

        // when
        ReflectionUtils.toSuitableCollectionType(rawType, set);
    }

    @Test
    public void shouldBeWellFormedUtilsClass() {
        TestUtils.assertIsProperUtilsClass(ReflectionUtils.class);
    }

    @Test
    public void shouldReturnEmptyMethodListForNoMethodScanClass() {
        assertThat(ReflectionUtils.safeGetDeclaredMethods(NoMethodScanClass.class), emptyArray());
        // Note: We need to check the size >= expected because plugins like Jacoco may add additional members
        assertThat(ReflectionUtils.safeGetDeclaredMethods(ReflectionsTestClass.class).length, greaterThanOrEqualTo(4));
    }

    @Test
    public void shouldReturnEmptyFieldListForNoFieldScanClass() {
        assertThat(ReflectionUtils.safeGetDeclaredFields(NoFieldScanClass.class), emptyArray());
        // Note: We need to check the size >= expected because plugins like Jacoco may add additional members
        assertThat(ReflectionUtils.safeGetDeclaredFields(ReflectionsTestClass.class).length, greaterThanOrEqualTo(4));
    }

    private static Field getField(String name) {
        try {
            return ReflectionsTestClass.class.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Method getMethod(String name, Class<?>... params) {
        try {
            return ReflectionsTestClass.class.getDeclaredMethod(name, params);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Matcher<? super Class<?>> isClass(Class clazz) {
        return equalTo(clazz);
    }

    // -------
    // Sample classes
    // -------
    @NoMethodScan
    private static final class NoMethodScanClass {
        private void someMethod() {
        }

        private boolean otherMethod() {
            return false;
        }
    }

    @NoFieldScan
    private static final class NoFieldScanClass {
        private byte oneField;
        private boolean twoField;
        private char threeField;
    }
}
