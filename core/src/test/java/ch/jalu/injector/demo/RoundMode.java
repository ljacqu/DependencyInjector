package ch.jalu.injector.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Sample annotation to demonstrate the use of custom annotations.
 * <p>
 * Identifying a value to be injected by an annotation is recommended if it's some sort of value
 * stemming from the application's environment or some sort of configurable setting. This is especially
 * useful for common types where you may want to pass multiple different objects which have different meanings.
 * For example, you may want to use various {@link java.io.File} instances as to pass the application's
 * data folder, messages folder, etc. This also adds more meaning to the value that you are passing and makes it
 * easily recognizable when the same value is being used in different classes.
 * <p>
 * Remember to <b>set the retention to RUNTIME</b> on the annotation or the injector will not be able to see it.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.PARAMETER})
public @interface RoundMode {
    // Note: it would have made more sense to put the RoundingMode in Settings as well. A custom annotation
    // is used here just to demonstrate this feature as well.
}
