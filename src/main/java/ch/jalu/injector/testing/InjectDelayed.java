package ch.jalu.injector.testing;

import ch.jalu.injector.testing.runner.DelayedInjectionRunner;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks fields to instantiate with mocks after {@link BeforeInjecting} methods.
 *
 * @see DelayedInjectionRunner
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface InjectDelayed {
}
