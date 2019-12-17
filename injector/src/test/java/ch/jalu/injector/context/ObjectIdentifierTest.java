package ch.jalu.injector.context;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.dependency.providers.Charlie;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.Size;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.List;
import java.util.Optional;

import static ch.jalu.injector.InjectorTestHelper.newDurationAnnotation;
import static ch.jalu.injector.InjectorTestHelper.newSizeAnnotation;
import static ch.jalu.injector.TestUtils.createParameterizedType;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link ObjectIdentifier}.
 */
class ObjectIdentifierTest {

    @Test
    void shouldReturnValues() {
        // given
        ObjectIdentifier identifier = new ObjectIdentifier(StandardResolutionType.REQUEST_SCOPED, Charlie.class,
            newSizeAnnotation("3245"), newDurationAnnotation());

        // when / then
        assertThat(identifier.getResolutionType(), equalTo(StandardResolutionType.REQUEST_SCOPED));
        assertThat(identifier.getType(), equalTo(Charlie.class));
        assertThat(identifier.getTypeAsClass(), equalTo(Charlie.class));
        assertThat(identifier.getAnnotations(), contains(instanceOf(Size.class), instanceOf(Duration.class)));
    }

    @Test
    void shouldReturnParameterizedTypeAsClass() {
        // given
        Type type1 = createParameterizedType(List.class, Double.class);
        ObjectIdentifier identifier1 = new ObjectIdentifier(null, type1);
        Type type2 = createParameterizedType(Optional.class, createParameterizedType(Comparable.class, String.class));
        ObjectIdentifier identifier2 = new ObjectIdentifier(null, type2);

        // when / then
        assertThat(identifier1.getTypeAsClass(), equalTo(List.class));
        assertThat(identifier2.getTypeAsClass(), equalTo(Optional.class));
    }

    @Test
    void shouldThrowForUnknownRawParameterizedType() {
        // given
        Type type = createParameterizedType(createParameterizedType(Object.class), Object.class);
        ObjectIdentifier identifier = new ObjectIdentifier(null, type);

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> identifier.getTypeAsClass());

        // then
        assertThat(ex.getMessage(), containsString("does not have a Class as its raw type"));
    }

    @Test
    void shouldThrowForUnknownTypeImplementation() {
        // given
        Type type = new WildcardTypeImpl();
        ObjectIdentifier identifier = new ObjectIdentifier(null, type);

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> identifier.getTypeAsClass());

        // then
        assertThat(ex.getMessage(), containsString("Unknown type"));
    }

    private static final class WildcardTypeImpl implements WildcardType {
        @Override
        public Type[] getUpperBounds() {
            return new Type[0];
        }

        @Override
        public Type[] getLowerBounds() {
            return new Type[0];
        }
    }
}