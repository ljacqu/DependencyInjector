package ch.jalu.injector.testing.runner;

import ch.jalu.injector.testing.DelayedInjectionRunnerIntegrationTest;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.testing.SampleInjectClass;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.jupiter.api.Test;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link RunDelayedInjects}.
 */
class RunDelayedInjectsTest {

    @Test
    void shouldInitializeInjectDelayedField() throws Throwable {
        // given - test class with initialized @Mock fields
        DelayedInjectionRunnerIntegrationTest runnerTest = new DelayedInjectionRunnerIntegrationTest();
        MockitoAnnotations.initMocks(runnerTest);

        TestClass testClass = new TestClass(runnerTest.getClass());
        Statement nextStatement = mock(Statement.class);
        List<FrameworkField> injectDelayedFields = testClass.getAnnotatedFields(InjectDelayed.class);
        assertThat(injectDelayedFields, hasSize(1)); // assumption
        RunDelayedInjects runDelayedInjects =
            new RunDelayedInjects(nextStatement, testClass, runnerTest, injectDelayedFields);

        // when
        runDelayedInjects.evaluate();

        // then
        assertThat(ReflectionUtils.getFieldValue(injectDelayedFields.get(0).getField(), runnerTest), not(nullValue()));
        verify(nextStatement).evaluate();
    }

    @Test
    void shouldThrowForAlreadyInitializedField() throws Throwable {
        // given - test class with initialized @Mock fields
        DelayedInjectionRunnerIntegrationTest runnerTest = new DelayedInjectionRunnerIntegrationTest();
        MockitoAnnotations.initMocks(runnerTest);

        TestClass testClass = new TestClass(runnerTest.getClass());
        Statement nextStatement = mock(Statement.class);
        List<FrameworkField> injectDelayedFields = testClass.getAnnotatedFields(InjectDelayed.class);
        assertThat(injectDelayedFields, hasSize(1)); // assumption
        ReflectionUtils.setField(injectDelayedFields.get(0).getField(), runnerTest, mock(SampleInjectClass.class));
        RunDelayedInjects runDelayedInjects =
                new RunDelayedInjects(nextStatement, testClass, runnerTest, injectDelayedFields);

        // when
        try {
            runDelayedInjects.evaluate();
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("Field with @InjectDelayed must be null"));
        }
    }
}