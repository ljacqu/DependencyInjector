package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;

import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Describes a dependency.
 */
public class DependencyDescription {

    private final Type type;
    private final Annotation[] annotations;

    public DependencyDescription(Type type, Annotation... annotations) {
        this.type = type;
        this.annotations = annotations;
    }

    /**
     * Returns the dependency's type. In general this is the type as returned by
     * {@link java.lang.reflect.Field#getGenericType()} or similar: it is a {@code Type}, which may be a
     * {@link Class} for simple types or types where no generic information is provided, or it may be a
     * {@link ParameterizedType} for types with generic information.
     *
     * Other extensions of Type are currently not supported.
     * <p>
     * Use {@link #getTypeAsClass()} to get the type as a Class safely.
     * See {@link ch.jalu.injector.utils.ReflectionUtils} for performing further queries on the type.
     *
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the dependency's type as a class.
     *
     * @return the type (as Class)
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
        throw new InjectorException("Unknown Type '" + type + "' (" + type.getClass()
            + ") cannot be converted to Class");
    }

    /**
     * Returns the annotations associated with the dependency declaration.
     * Empty array if no annotations are present.
     *
     * @return the annotations
     */
    public Annotation[] getAnnotations() {
        return annotations;
    }

}
