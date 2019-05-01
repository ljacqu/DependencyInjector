package ch.jalu.injector.testing.extension;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class ExtensionUtils {

    private ExtensionUtils() {
    }

    public static List<Field> getAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAnnotatedMembers(clazz, Class::getDeclaredFields, annotation);
    }

    public static List<Method> getAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotation) {
        return getAnnotatedMembers(clazz, Class::getDeclaredMethods, annotation);
    }

    private static <M extends AnnotatedElement> List<M> getAnnotatedMembers(Class<?> clazz,
                                                                            Function<Class<?>, M[]> membersFn,
                                                                            Class<? extends Annotation> annotation) {
        List<M> fields = new ArrayList<>();
        Class<?> currentClass = clazz;
        while (currentClass != Object.class && currentClass != null) {
            for (M annotatedElement : membersFn.apply(currentClass)) {
                if (annotatedElement.isAnnotationPresent(annotation)) {
                    fields.add(annotatedElement);
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return fields;
    }
}
