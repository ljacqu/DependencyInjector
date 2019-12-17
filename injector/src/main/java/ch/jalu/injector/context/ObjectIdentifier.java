package ch.jalu.injector.context;

import ch.jalu.injector.exceptions.InjectorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * Identifies objects.
 */
public class ObjectIdentifier {

    private final ResolutionType resolutionType;
    private final Type type;
    private final List<Annotation> annotations;

    public ObjectIdentifier(ResolutionType resolutionType, Type type, Annotation... annotations) {
        this.resolutionType = resolutionType;
        this.type = type;
        this.annotations = Arrays.asList(annotations);
    }

    /**
     * @return the resolution type (scope) requested for the object
     */
    public ResolutionType getResolutionType() {
        return resolutionType;
    }

    /**
     * Returns the type of the requested object. In general this is the type as returned by
     * {@link java.lang.reflect.Field#getGenericType()} or similar: it is a {@code Type}, which may be a
     * {@link Class} for simple types or types where no generic information is provided, or it may be a
     * {@link ParameterizedType} for types with generic information.
     *
     * Other extensions of Type are not supported by default.
     * <p>
     * Use {@link #getTypeAsClass()} to get the type as a Class safely.
     * See {@link ch.jalu.injector.utils.ReflectionUtils} for performing further operations on the type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the type of the requested object as a Class. If the {@link #getType type} is a type with
     * generic information, the returned class is the raw type without the generic information
     * (e.g. {@code List} if the type represents {@code List<String>}).
     *
     * @return the type as class
     */
    public Class<?> getTypeAsClass() {
        if (type instanceof Class<?>) {
            return (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (rawType instanceof Class<?>) {
                return (Class<?>) rawType;
            } else {
                throw new InjectorException("Parameterized type '" + type + "' does not have a Class as its raw type");
            }
        }
        throw new InjectorException("Unknown type '" + type + "' (" + type.getClass()
            + ") cannot be converted to Class");
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return "ObjId[type=" + type + ", annotations=" + annotations + "]";
    }
}
