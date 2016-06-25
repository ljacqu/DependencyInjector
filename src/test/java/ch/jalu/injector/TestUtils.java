package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.util.Objects;

import static org.hamcrest.Matchers.containsString;

/**
 * Utility class for testing.
 */
public class TestUtils {

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

    public static final class ExceptionCatcher {
        private final ExpectedException expectedException;

        public ExceptionCatcher(ExpectedException expectedException) {
            this.expectedException = expectedException;
        }

        public void expect(String message, Class<?> contextClass) {
            expectedException.expect(InjectorException.class);
            expectedException.expectMessage(containsString(message));
            expectedException.expect(hasClass(contextClass));
        }

        private static <T extends InjectorException> Matcher<T> hasClass(final Class<?> clazz) {
            return new TypeSafeMatcher<T>() {
                @Override
                public void describeTo(Description description) {
                    description.appendText("Expected exception with class '" + clazz.getSimpleName() + "'");
                }

                @Override
                public void describeMismatchSafely(T item, Description mismatchDescription) {
                    String className = item.getClazz() == null ? "null" : item.getClazz().getSimpleName();
                    mismatchDescription.appendText("had class '" + className + "'");
                }

                @Override
                protected boolean matchesSafely(T item) {
                    return Objects.equals(clazz, item.getClazz());
                }
            };
        }
    }

}
