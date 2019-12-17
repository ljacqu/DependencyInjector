package ch.jalu.injector;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Utility class for testing.
 */
public final class TestUtils {

    private TestUtils() {
    }

    /**
     * Returns a custom matcher that checks an annotation's {@link Annotation#annotationType() type}
     * against the given argument.
     *
     * @param type the type to verify against
     * @return generated matcher
     */
    public static Matcher<? super Annotation> annotationOf(final Class<?> type) {
        return new TypeSafeMatcher<Annotation>() {
            @Override
            protected boolean matchesSafely(Annotation item) {
                return item.annotationType().equals(type);
            }

            @Override
            public void describeTo(Description description) {
                description.appendValue("Annotation of type @" + type.getSimpleName());
            }
        };
    }

    public static <T> T findOrThrow(Collection<T> coll, Predicate<? super T> predicate) {
        return coll.stream().filter(predicate).findFirst()
            .orElseThrow(() -> new IllegalStateException("Could not find any matching item"));
    }

    public static ParameterizedType createParameterizedType(Type rawType, Type... actualTypeArguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return actualTypeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                throw new UnsupportedOperationException();
            }
        };
    }
}
