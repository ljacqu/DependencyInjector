package ch.jalu.injector.testing.runner;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.testing.DelayedInjectionRunnerIntegrationTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.TestClass;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link MockDependencyHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class MockDependencyHandlerTest {

    @Mock
    private Injector injector;

    @Test
    public void shouldProvideMock() throws Exception {
        // given
        DelayedInjectionRunnerIntegrationTest runnerTest = new DelayedInjectionRunnerIntegrationTest();
        MockitoAnnotations.initMocks(runnerTest);
        TestClass testClass = new TestClass(runnerTest.getClass());
        MockDependencyHandler mockDependencyHandler = new MockDependencyHandler(testClass, runnerTest);
        DependencyDescription dependencyDescription = new DependencyDescription(AlphaService.class, null, null);
        AlphaService alphaService = mock(AlphaService.class);
        given(injector.getIfAvailable(AlphaService.class)).willReturn(alphaService);

        // when
        Object value = mockDependencyHandler.resolveValue(injector, dependencyDescription);

        // then
        assertThat(value == alphaService, equalTo(true));
        verify(injector).register(eq(AlphaService.class), any(AlphaService.class));
        verify(injector).register(eq(ClassWithAbstractDependency.AbstractDependency.class),
            any(ClassWithAbstractDependency.AbstractDependency.class));
        verify(injector).getIfAvailable(AlphaService.class);
    }

    @Test
    public void shouldThrowForUnavailableMock() {
        // given
        DelayedInjectionRunnerIntegrationTest runnerTest = new DelayedInjectionRunnerIntegrationTest();
        MockitoAnnotations.initMocks(runnerTest);
        TestClass testClass = new TestClass(runnerTest.getClass());
        MockDependencyHandler mockDependencyHandler = new MockDependencyHandler(testClass, runnerTest);
        DependencyDescription dependencyDescription = new DependencyDescription(AlphaService.class, null, null);
        given(injector.getIfAvailable(AlphaService.class)).willReturn(null);

        // when
        try {
            mockDependencyHandler.resolveValue(injector, dependencyDescription);
            fail("Expected exception to be thrown");
        } catch (Exception e) {
            assertThat(e.getMessage(), containsString("dependencies of @InjectDelayed must be provided as @Mock"));
            verify(injector, times(2)).register(any(Class.class), any(Object.class));
            verify(injector).getIfAvailable(AlphaService.class);
        }
    }

}