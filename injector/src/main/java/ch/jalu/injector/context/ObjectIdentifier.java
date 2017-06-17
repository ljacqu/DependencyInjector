package ch.jalu.injector.context;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

/**
 * Identifies objects.
 */
public class ObjectIdentifier {

    private final Class<?> type;
    private final List<Annotation> annotations;

    public ObjectIdentifier(Class<?> type, Annotation... annotations) {
        this.type = type;
        this.annotations = Arrays.asList(annotations);
    }

    public Class<?> getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return annotations;
    }
}
