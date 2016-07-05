package ch.jalu.injector.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sets a field with all known classes extending the given {@code value}. The field must be a collection or
 * array of {@code Class} type. The handler for this annotation must be provided explicitly; it is not part of the
 * {@link ch.jalu.injector.InjectorBuilder#createDefaultHandlers(String) default handlers}.
 *
 * @see ch.jalu.injector.handlers.dependency.AllTypesAnnotationHandler
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllTypes {

    /**
     * @return the parent class to get all known subtypes of
     */
    Class<?> value();

}
