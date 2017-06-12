package ch.jalu.injector.extras;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that will be initialized to all found subtypes of the field's type. Must be a collection or
 * an array. For example:
 * <pre>
 * {@code @Inject}
 * {@code @AlInstances}
 * {@code private Set<Command> commands;}
 * </pre>
 *
 * This will initialize the field with all known subtypes of {@code Command} in the project. The handler
 * for this annotation must be provided explicitly; it is not part of the
 * {@link ch.jalu.injector.InjectorBuilder#createDefaultHandlers(String) default handlers}.
 *
 * @see ch.jalu.injector.handlers.dependency.AllInstancesAnnotationHandler
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllInstances {

}
