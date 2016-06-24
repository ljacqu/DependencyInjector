package ch.jalu.injector.annotationhandlers;

import ch.jalu.injector.Injector;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Simple injector for annotation handlers. See {@link AnnotationHandler} for supported field types.
 */
public class AnnotationHandlerInjector {

    private final String rootPackage;
    private final Injector injector;

    public AnnotationHandlerInjector(String rootPackage, Injector injector) {
        this.rootPackage = rootPackage;
        this.injector = injector;
    }

    public void inject(List<? extends AnnotationHandler> handlers) {
        for (AnnotationHandler handler : handlers) {
            injectFields(handler);
        }
    }

    private void injectFields(AnnotationHandler handler) {
        List<Field> fields = ReflectionUtils.getFieldsWithAnnotation(handler.getClass(), Inject.class);
        for (Field field : fields) {
            ReflectionUtils.setField(field, handler, getValueForField(field));
        }
    }

    private Object getValueForField(Field field) {
        Class<?> type = field.getType();
        if (type == Injector.class) {
            return injector;
        } else if (type == String.class) {
            return rootPackage;
        }
        throw new InjectorException("Found @Inject field '" + field.getName() + "' in '"
            + field.getDeclaringClass() + "' of unsupported type", field.getDeclaringClass());
    }

}
