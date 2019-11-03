package ch.jalu.injector.utils;

import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.annotations.NoMethodScan;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.exceptions.InjectorReflectionException;
import org.junit.Test;

import javax.inject.Provider;
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

    @Test
    public void shouldGetFieldValue() {
        // given
        String str = "string value sample";
        ReflectionsTestClass testClass = new ReflectionsTestClass(str, 123);
        Field field = getField("string");

        // when
        Object result = ReflectionUtils.getFieldValue(field, testClass);

        // then
        assertThat(result, equalTo(str));
    }

    @Test(expected = InjectorReflectionException.class)
    public void shouldForwardException() throws NoSuchFieldException {
        // given
        ReflectionsTestClass testClass = new ReflectionsTestClass("abc", 123);
        Field field = GenericTypesClass.class.getDeclaredField("boolList");

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

    @Test(expected = InjectorReflectionException.class)
    public void shouldForwardExceptionWhenSettingField() {
        // given
        Field field = getField("integer");

        // when
        ReflectionUtils.setField(field, new Object(), 3);
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
            .getDeclaredConstructor(int.class);

        // when
        ReflectionsTestClass testClass = ReflectionUtils.newInstance(constr, 543);

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
        Class<?> result = ReflectionUtils.getCollectionType(mainType, null);

        // then
        assertThat(result, equalTo(Exception.class));
    }

    @Test
    public void shouldReturnNullForNonGenericClass() {
        // given
        Class<?> mainType = Exception.class;
        Type genericType = mock(ParameterizedType.class);

        // when
        Class<?> result = ReflectionUtils.getCollectionType(mainType, genericType);

        // then
        assertThat(result, nullValue());
        verifyZeroInteractions(genericType);
    }

    @Test
    public void shouldReturnGenericTypeOfList() throws NoSuchFieldException {
        // given
        Field boolList = GenericTypesClass.class.getDeclaredField("boolList");

        // when
        Class<?> result = ReflectionUtils.getCollectionType(boolList.getType(), boolList.getGenericType());

        // then
        assertThat(result, equalTo(Boolean.class));
    }

    @Test
    public void shouldNotGetGenericTypeIfNotIterableSubtype() throws NoSuchFieldException {
        // given
        // take genericType from the List<Boolean> field again, avoids us having to mock
        Field boolList = GenericTypesClass.class.getDeclaredField("boolList");

        // when
        Class<?> result = ReflectionUtils.getCollectionType(Class.class, boolList.getGenericType());

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

    @Test
    public void shouldGetTypedValue() throws ReflectiveOperationException {
        // given
        Field field = GenericTypesClass.class.getDeclaredField("stringProvider");
        Type constrParam = GenericTypesClass.class.getDeclaredConstructor(Set.class, List.class)
            .getGenericParameterTypes()[0];
        Type methodParam = GenericTypesClass.class.getDeclaredMethod("withTypeList", List.class)
            .getGenericParameterTypes()[0];

        // when
        Class<?> fieldType = ReflectionUtils.getGenericType(field.getGenericType());
        Class<?> constrParamType = ReflectionUtils.getGenericType(constrParam);
        Class<?> methodParamType = ReflectionUtils.getGenericType(methodParam);

        // then
        assertThat(fieldType, equalTo(String.class));
        assertThat(constrParamType, equalTo(Integer.class));
        assertThat(methodParamType, equalTo(Type.class));
    }

    @Test
    public void shouldReturnNullForNotPresentGenerics() throws ReflectiveOperationException {
        // given
        Field field1 = GenericTypesClass.class.getDeclaredField("untypedIterable");
        Field field2 = GenericTypesClass.class.getDeclaredField("booleanField");
        Type untypedParam = GenericTypesClass.class.getDeclaredMethod("withUntypedSet", Set.class)
            .getGenericParameterTypes()[0];

        // when
        Class<?> fieldType1 = ReflectionUtils.getGenericType(field1.getGenericType());
        Class<?> fieldType2 = ReflectionUtils.getGenericType(field2.getGenericType());
        Class<?> paramType = ReflectionUtils.getGenericType(untypedParam);

        // then
        assertThat(fieldType1, nullValue());
        assertThat(fieldType2, nullValue());
        assertThat(paramType, nullValue());
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

    private static final class GenericTypesClass {
        private Provider<String> stringProvider;
        private List<Boolean> boolList;
        private Iterable<?> untypedIterable;
        private boolean booleanField;

        GenericTypesClass(Set<Integer> intSet, List<?> untypedLiteral) {
        }

        private void withTypeList(List<Type> typeList) {
        }
        private void withUntypedSet(Set set) {
        }
    }
}
