package ch.jalu.injector.utils;

/**
 * Test class with various fields, methods and
 */
public class ReflectionsTestClass {

    private static Object staticObject;
    private String string;
    private Integer integer;
    private final int finalInt = 3;

    private ReflectionsTestClass(String s) {
        string = s;
    }

    public ReflectionsTestClass(String str, int i) {
        string = str;
        integer = i;
    }

    // Throwing constructor
    private ReflectionsTestClass(boolean b) {
        throw new UnsupportedOperationException("Test constructor throwing exception");
    }


    private void setIntegerField(Integer arg) {
        integer = arg;
    }

    private Integer getIntegerDouble() {
        return integer == null ? null : integer * 2;
    }

    public static Object changeStaticObject(Object o) {
        Object old = staticObject;
        staticObject = o;
        return old;
    }

    private void throwingMethod() {
        throw new IllegalStateException("Test method throwing exception");
    }

    public Integer getInteger() {
        return integer;
    }
}
