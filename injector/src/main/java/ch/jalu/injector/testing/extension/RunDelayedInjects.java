package ch.jalu.injector.testing.extension;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Statement for initializing {@link ch.jalu.injector.testing.InjectDelayed} fields. These fields are
 * constructed after {@link ch.jalu.injector.testing.BeforeInjecting} and before JUnit's &#064;Before.
 */
public class RunDelayedInjects {

    private Object target;

    public RunDelayedInjects(Object target) {
        this.target = target;
    }

    public void evaluate() {
        Injector injector = getInjector();
        for (Field field : ExtensionUtils.getAnnotatedFields(target.getClass(), InjectDelayed.class)) {
            if (ReflectionUtils.getFieldValue(field, target) != null) {
                throw new IllegalStateException("Field with @InjectDelayed must be null on startup. "
                    + "Field '" + field.getName() + "' is not null");
            }
            Object object = injector.getSingleton(field.getType());
            ReflectionUtils.setField(field, target, object);
        }
    }

    /**
     * Override this method to provide your own injector in the test runner, e.g. if your application uses
     * custom instantiation methods or annotation behavior.
     *
     * @return the injector used to set {@link ch.jalu.injector.testing.InjectDelayed} fields
     */
    protected Injector getInjector() {
        List<Handler> instantiationProviders = InjectorBuilder.createInstantiationProviders("");
        return new InjectorBuilder()
            .addHandlers(
                new AnnotationResolver(target),
                new MockDependencyHandler(target),
                new PostConstructMethodInvoker())
            .addHandlers(instantiationProviders)
            .create();
    }
}
