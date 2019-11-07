package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.Size;
import org.junit.jupiter.api.Test;

import java.lang.annotation.Annotation;

import static ch.jalu.injector.InjectorTestHelper.newDurationAnnotation;
import static ch.jalu.injector.InjectorTestHelper.newSizeAnnotation;
import static ch.jalu.injector.InjectorTestHelper.unwrapFromSimpleResolution;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void shouldThrowForSecondAnnotationRegistration() {
        // given
        savedAnnotationsHandler.onAnnotation(Size.class, 12);

        // when / then
        assertThrows(InjectorException.class,
            () -> savedAnnotationsHandler.onAnnotation(Size.class, -8));
    }

    @Test
    public void shouldThrowForNullValueAssociatedToAnnotation() {
        // given / when / then
        assertThrows(InjectorException.class,
            () -> savedAnnotationsHandler.onAnnotation(Duration.class, null));
    }
}