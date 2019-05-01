package ch.jalu.injector.testing.extension;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import static ch.jalu.injector.testing.extension.ExtensionUtils.getAnnotatedFields;

/**
 * Resolves a dependency by retrieving the value from a corresponding @Mock field.
 * Throws an exception if a dependency is not available as {@code @Mock} field.
 */
public class MockDependencyHandler implements Handler {

    private final Object target;
    private boolean areMocksRegistered;
    private Set<Class<?>> fieldsToInject;

    public MockDependencyHandler(Object target) {
        this.target = target;
    }

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        final Injector injector = context.getInjector();
        if (!areMocksRegistered) {
            registerAllMocks(injector);
            areMocksRegistered = true;
        }

        Class<?> type = context.getIdentifier().getTypeAsClass();
        Object object = injector.getIfAvailable(type);
        if (object != null) {
            return new SimpleResolution<>(object);
        }
        if (fieldsToInject.contains(type)) {
            // The required type is present as @InjectDelayed. Return null to make the injector instantiate the type
            return null;
        }

        // Throw exception: type is not present as @Mock or @InjectDelayed
        throw new InjectorException("No mock found for '" + type + "'. "
            + "All dependencies of @InjectDelayed must be provided as @Mock or @InjectDelayed fields");
    }

    /**
     * Registers all mocks in the injector. Note that null values or multiple fields of the same type
     * will cause the injector to throw an exception.
     *
     * @param injector the injector to use
     */
    private void registerAllMocks(Injector injector) {
        for (Field field : getAnnotatedFields(target.getClass(), Mock.class)) {
            // Unchecked so we don't need to cast the field's value...
            Class clazz = field.getType();
            injector.register(clazz, ReflectionUtils.getFieldValue(field, target));
        }

        fieldsToInject = new HashSet<>();
        for (Field field : getAnnotatedFields(target.getClass(), InjectDelayed.class)) {
            fieldsToInject.add(field.getType());
        }
    }

}
