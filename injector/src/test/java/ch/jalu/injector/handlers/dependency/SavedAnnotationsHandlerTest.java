package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.Size;
import org.junit.Test;

import java.lang.annotation.Annotation;

import static ch.jalu.injector.ResolutionTestHelper.unwrapFromSimpleResolution;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SavedAnnotationsHandler}.
 */
public class SavedAnnotationsHandlerTest {

    private SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();

    @Test
    public void shouldReturnRegisteredValue() {
        // given
        Object object = "value for @Duration";
        savedAnnotationsHandler.onAnnotation(Duration.class, object);
        Annotation[] annotations = {
                newSizeAnnotation("value"), newDurationAnnotation()
        };
        ResolutionContext context = new ResolutionContext(
            null, new ObjectIdentifier(null, null, annotations));

        // when
        // Injector param not needed -> null
        Resolution<?> instantiation = savedAnnotationsHandler.resolve(context);

        // then
        assertThat(unwrapFromSimpleResolution(instantiation), equalTo(object));
    }

    @Test
    public void shouldReturnNullForUnregisteredAnnotation() {
        // given
        Annotation[] annotations = {
            newSizeAnnotation("value"), newDurationAnnotation()
        };
        ResolutionContext context = new ResolutionContext(
            null, new ObjectIdentifier(null, null, annotations));
        // register some object under another annotation for the heck of it
        savedAnnotationsHandler.onAnnotation(Test.class, new Object());

        // when
        Resolution<?> result = savedAnnotationsHandler.resolve(context);

        // then
        assertThat(result, nullValue());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForSecondAnnotationRegistration() {
        // given
        savedAnnotationsHandler.onAnnotation(Size.class, 12);

        // when
        savedAnnotationsHandler.onAnnotation(Size.class, -8);

        // then - exception
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForNullValueAssociatedToAnnotation() {
        // given / when
        savedAnnotationsHandler.onAnnotation(Duration.class, null);

        // then - exception
    }

    private static Size newSizeAnnotation(final String value) {
        return new Size() {
            @Override
            public Class<Size> annotationType() {
                return Size.class;
            }

            @Override
            public String value() {
                return value;
            }
        };
    }

    private static Duration newDurationAnnotation() {
        return new Duration() {
            @Override
            public Class<Duration> annotationType() {
                return Duration.class;
            }
        };
    }

}