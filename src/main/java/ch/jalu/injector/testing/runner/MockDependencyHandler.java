package ch.jalu.injector.testing.runner;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

/**
 * Resolves a dependency by retrieving the value from a corresponding @Mock field.
 * Throws an exception if a dependency is not available as {@code @Mock} field.
 */
public class MockDependencyHandler implements DependencyHandler {

    private final TestClass testClass;
    private final Object target;
    private boolean areMocksRegistered;
    private Set<Class<?>> fieldsToInject;

    public MockDependencyHandler(TestClass testClass, Object target) {
        this.testClass = testClass;
        this.target = target;
    }

    @Override
    public Object resolveValue(Injector injector, DependencyDescription dependencyDescription) throws Exception {
        if (!areMocksRegistered) {
            registerAllMocks(injector);
            areMocksRegistered = true;
        }

        Class<?> type = dependencyDescription.getType();
        Object object = injector.getIfAvailable(type);
        if (object != null) {
            return object;
        }
        if (fieldsToInject.contains(type)) {
            // The required type is present as @InjectDelayed. Return null to make the injector instantiate the type
            return null;
        }

        // Throw exception: type is not present as @Mock or @InjectDelayed
        throw new IllegalStateException("No mock found for '" + type + "'. "
            + "All dependencies of @InjectDelayed must be provided as @Mock or @InjectDelayed fields");
    }

    /**
     * Registers all mocks in the injector. Note that null values or multiple fields of the same type
     * will cause the injector to throw an exception.
     *
     * @param injector the injector to use
     */
    private void registerAllMocks(Injector injector) {
        for (FrameworkField frameworkField : testClass.getAnnotatedFields(Mock.class)) {
            // Unchecked so we don't need to cast the field's value...
            Class clazz = frameworkField.getType();
            injector.register(clazz, ReflectionUtils.getFieldValue(frameworkField.getField(), target));
        }

        fieldsToInject = new HashSet<>();
        for (FrameworkField frameworkField : testClass.getAnnotatedFields(InjectDelayed.class)) {
            fieldsToInject.add(frameworkField.getType());
        }
    }

}
