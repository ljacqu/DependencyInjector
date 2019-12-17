package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.InjectorTestHelper;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.samples.Size;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;

import static ch.jalu.injector.InjectorTestHelper.newDurationAnnotation;
import static ch.jalu.injector.InjectorTestHelper.newSizeAnnotation;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link TypeSafeAnnotationHandler}.
 */
class TypeSafeAnnotationHandlerTest {

    @Test
    void shouldCallResolveSafelyMethod() throws Exception {
        // given
        ObjectIdentifier identifier = new ObjectIdentifier(null, String.class,
            newDurationAnnotation(), newSizeAnnotation("0.52"));
        ResolutionContext context = new ResolutionContext(null, identifier);
        TypeSafeAnnotationHandlerTestImpl handler = new TypeSafeAnnotationHandlerTestImpl();

        // when
        Resolution<?> resolution = handler.resolve(context);

        // then
        assertThat(InjectorTestHelper.unwrapFromSimpleResolution(resolution), equalTo("demo"));
        assertThat(handler.value, equalTo("0.52"));
    }

    @Test
    void shouldNotForwardToHandlerIfIrrelevant() throws Exception {
        // given
        ObjectIdentifier identifier = new ObjectIdentifier(null, String.class, newDurationAnnotation());
        ResolutionContext context = new ResolutionContext(null, identifier);
        TypeSafeAnnotationHandlerTestImpl handler = new TypeSafeAnnotationHandlerTestImpl();

        // when
        Resolution<?> resolution = handler.resolve(context);

        // then
        assertThat(resolution, nullValue());
        assertThat(handler.value, nullValue());
    }

    @Test
    void shouldHandleContextWithNoAnnotations() throws Exception {
        // given
        ObjectIdentifier identifier = new ObjectIdentifier(null, String.class);
        ResolutionContext context = new ResolutionContext(null, identifier);
        TypeSafeAnnotationHandlerTestImpl handler = new TypeSafeAnnotationHandlerTestImpl();

        // when
        Resolution<?> resolution = handler.resolve(context);

        // then
        assertThat(resolution, nullValue());
        assertThat(handler.value, nullValue());
    }

    private static final class TypeSafeAnnotationHandlerTestImpl extends TypeSafeAnnotationHandler<Size> {

        private String value;

        @Override
        protected Class<Size> getAnnotationType() {
            return Size.class;
        }

        @Nullable
        @Override
        protected Resolution<?> resolveValueSafely(ResolutionContext context, Size annotation) {
            value = annotation.value();
            return new SimpleResolution<>("demo");
        }
    }
}