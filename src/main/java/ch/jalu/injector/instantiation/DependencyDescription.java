package ch.jalu.injector.instantiation;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Describes a dependency.
 */
public class DependencyDescription {

    private final Class<?> type;
    private final Type genericType;
    private final Annotation[] annotations;

    public DependencyDescription(Class<?> type, Type genericType, Annotation... annotations) {
        this.type = type;
        this.genericType = genericType;
        this.annotations = annotations;
    }

    /**
     * Returns the dependency's type.
     *
     * @return the type (class)
     */
    public Class<?> getType() {
        return type;
    }

    /**
     * Returns the dependency's generic type if applicable.
     *
     * @return the generic type or {@code null} if not applicable
     */
    @Nullable
    public Type getGenericType() {
        return genericType;
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
