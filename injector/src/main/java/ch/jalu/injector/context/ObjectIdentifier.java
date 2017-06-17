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

    private final Type type;
    private final List<Annotation> annotations;

    public ObjectIdentifier(Type type, Annotation... annotations) {
        this.type = type;
        this.annotations = Arrays.asList(annotations);
    }

    public Type getType() {
        return type;
    }

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

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
