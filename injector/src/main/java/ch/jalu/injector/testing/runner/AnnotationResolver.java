package ch.jalu.injector.testing.runner;

import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Resolves dependency by annotation: if a field is present in the test class with the same annotation as
 * on the dependency, the field will be used if the type matches.
 */
public class AnnotationResolver implements Handler {

    private final TestClass testClass;
    private final Object target;
    private final Set<Class<? extends Annotation>> ignoredAnnotations;

    public AnnotationResolver(TestClass testClass, Object target) {
        this(testClass, target, Inject.class, InjectMocks.class, Mock.class, Spy.class, InjectDelayed.class);
    }

    @SafeVarargs
    public AnnotationResolver(TestClass testClass, Object target, Class<? extends Annotation>... ignoredAnnotations) {
        this.testClass = testClass;
        this.target = target;
        this.ignoredAnnotations = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(ignoredAnnotations)));
    }

    @Override
    public Object resolveValue(ResolvedContext context,
                               DependencyDescription dependencyDescription) throws Exception {
        for (Annotation annotation : dependencyDescription.getAnnotations()) {
            Object o = resolveByAnnotation(annotation.annotationType(), dependencyDescription.getTypeAsClass());
            if (o != null) {
                return o;
            }
        }
        return null;
    }

    @Nullable
    private Object resolveByAnnotation(Class<? extends Annotation> annotation, Class<?> type) {
        if (!ignoredAnnotations.contains(annotation)) {
            List<FrameworkField> fields = testClass.getAnnotatedFields(annotation);
            for (FrameworkField field : fields) {
                if (type.isAssignableFrom(field.getType())) {
                    return ReflectionUtils.getFieldValue(field.getField(), target);
                }
            }
        }
        return null;
    }
}
