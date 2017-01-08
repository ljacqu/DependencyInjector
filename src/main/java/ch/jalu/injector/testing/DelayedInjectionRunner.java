package ch.jalu.injector.testing;

import ch.jalu.injector.testing.runner.DelayedInjectionRunnerValidator;
import ch.jalu.injector.testing.runner.RunBeforeInjectings;
import ch.jalu.injector.testing.runner.RunDelayedInjects;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.mockito.MockitoAnnotations;

import java.util.List;

/**
 * Custom JUnit runner which adds support for {@link InjectDelayed} and {@link BeforeInjecting}.
 * This runner also initializes fields with Mockito's {@link org.mockito.Mock}, {@link org.mockito.Spy} and
 * {@link org.mockito.InjectMocks}.
 * <p>
 * Mockito's {@link org.mockito.Mock} and {@link org.mockito.InjectMocks} are initialized <i>before</i>
 * {@link org.junit.Before} methods are run. This leaves no possibility to initialize some mock
 * behavior before {@link org.mockito.InjectMocks} fields get instantiated.
 * <p>
 * The runner fills this gap by introducing {@link BeforeInjecting}. At the time these methods
 * are run Mockito's annotation will have taken effect but not {@link InjectDelayed}. Fields with
 * this annotation are initialized after {@link BeforeInjecting} methods have been run.
 * <p>
 * Additionally, after a field annotated with {@link InjectDelayed} has been initialized, its
 * {@link javax.annotation.PostConstruct} method will be invoked, if available.
 * <p>
 * Important: It is required to declare all dependencies of classes annotated with
 * {@link InjectDelayed} as {@link org.mockito.Mock} fields. If a dependency is missing, an exception
 * will be thrown.
 */
public class DelayedInjectionRunner extends BlockJUnit4ClassRunner {

    public DelayedInjectionRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
    }

    @Override
    public Statement withBefores(FrameworkMethod method, Object target, Statement statement) {
        // Initialize all Mockito annotations
        MockitoAnnotations.initMocks(target);

        // Call chain normally: let parent handle @Before methods.
        // Note that the chain of statements will be run from the end to the start,
        // so @Before will be run AFTER our custom statements below
        statement = super.withBefores(method, target, statement);

        // Add support for @BeforeInjecting and @InjectDelayed (again, reverse order)
        statement = withDelayedInjects(target, statement);
        return withBeforeInjectings(target, statement);
    }

    @Override
    public void run(final RunNotifier notifier) {
        // add listener that validates framework usage at the end of each test
        notifier.addListener(new DelayedInjectionRunnerValidator(notifier, getTestClass()));
        super.run(notifier);
    }

    /* Adds a Statement to the chain if @BeforeInjecting methods are present. */
    private Statement withBeforeInjectings(Object target, Statement statement) {
        List<FrameworkMethod> beforeInjectings = getTestClass().getAnnotatedMethods(BeforeInjecting.class);
        return beforeInjectings.isEmpty()
            ? statement
            : new RunBeforeInjectings(statement, beforeInjectings, target);
    }

    /*
     * Adds a Statement to the chain if @InjectDelayed methods are present.
     * If fields have been found, the injection for the type is resolved and stored with the necessary dependencies.
     */
    private Statement withDelayedInjects(Object target, Statement statement) {
        List<FrameworkField> delayedFields = getTestClass().getAnnotatedFields(InjectDelayed.class);
        return delayedFields.isEmpty()
            ? statement
            : createDelayedInjectsStatement(target, statement, delayedFields);
    }

    /**
     * Creates a statement that will instantiate the fields annotated with {@link InjectDelayed}.
     * Override this method to provide another statement. For example, you can override
     * {@link RunDelayedInjects#getInjector()} to use a differently configured injector.
     *
     * @param target the object the test will be run on
     * @param statement the next statement
     * @param delayedFields fields annotated with {@link InjectDelayed}
     * @return the generated statement
     */
    protected Statement createDelayedInjectsStatement(Object target, Statement statement,
                                                      List<FrameworkField> delayedFields) {
        return new RunDelayedInjects(statement, getTestClass(), target, delayedFields);
    }
}
