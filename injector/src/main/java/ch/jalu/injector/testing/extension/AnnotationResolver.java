package ch.jalu.injector.testing.extension;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static ch.jalu.injector.testing.extension.ExtensionUtils.getAnnotatedFields;

/**
 * Resolves dependency by annotation: if a field is present in the test class with the same annotation as
 * on the dependency, the field will be used if the type matches.
 */
public class AnnotationResolver implements Handler {

    private final Object target;
    private final Set<Class<? extends Annotation>> ignoredAnnotations;

    public AnnotationResolver(Object target) {
        this(target, Inject.class, InjectMocks.class, Mock.class, Spy.class, InjectDelayed.class);
    }

    @SafeVarargs
    public AnnotationResolver(Object target, Class<? extends Annotation>... ignoredAnnotations) {
        this.target = target;
        this.ignoredAnnotations = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ignoredAnnotations)));
    }

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        final Class<?> clazz = context.getIdentifier().getTypeAsClass();
        for (Annotation annotation : context.getIdentifier().getAnnotations()) {
            Object o = resolveByAnnotation(annotation.annotationType(), clazz);
            if (o != null) {
                return new SimpleResolution<>(o);
            }
        }
        return null;
    }

    private Object resolveByAnnotation(Class<? extends Annotation> annotation, Class<?> type) {
        if (!ignoredAnnotations.contains(annotation)) {
            for (Field field : getAnnotatedFields(target.getClass(), annotation)) {
                if (type.isAssignableFrom(field.getType())) {
                    return ReflectionUtils.getFieldValue(field, target);
                }
            }
        }
        return null;
    }


}
