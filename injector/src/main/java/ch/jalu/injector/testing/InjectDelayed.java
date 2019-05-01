package ch.jalu.injector.testing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields to instantiate with mocks after {@link BeforeInjecting} methods.
 *
 * @see DelayedInjectionRunner test runner for JUnit 4
 * @see DelayedInjectionExtension test extension for JUnit 5
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectDelayed {
}
