package ch.jalu.injector.testing.extension;

import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.reflect.Method;

/**
 * Statement for running {@link ch.jalu.injector.testing.BeforeInjecting} methods. Such methods are run
 * after Mockito's &#064;Mock, &#064;Spy and &#064;InjectMocks have taken effect,
 * but before {@link ch.jalu.injector.testing.InjectDelayed} fields are handled.
 */
public class RunBeforeInjectings {

    private final Object target;

    public RunBeforeInjectings(Object target) {
        this.target = target;
    }

    public void evaluate() {
        for (Method method : ExtensionUtils.getAnnotatedMethods(target.getClass(), BeforeInjecting.class)) {
            ReflectionUtils.invokeMethod(method, target);
        }
    }
}
