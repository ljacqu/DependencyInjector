package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.annotations.NoMethodScan;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.PostConstructTestClass;
import ch.jalu.injector.samples.ProvidedClass;
import org.junit.jupiter.api.Test;

import javax.annotation.PostConstruct;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link PostConstructMethodInvoker}.
 */
public class PostConstructMethodInvokerTest {

    private PostConstructMethodInvoker postConstructInvoker = new PostConstructMethodInvoker();

    @Test
    public void shouldExecutePostConstructMethod() {
        // given
        PostConstructTestClass testClass = new PostConstructTestClass(123, new ProvidedClass(""));

        // when
        postConstructInvoker.postProcess(testClass, null, null);

        // then
        assertThat(testClass.wasPostConstructCalled(), equalTo(true));
    }

    @Test
    public void shouldThrowForInvalidPostConstructMethod() {
        // given
        WithParams withParams = new WithParams();

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> postConstructInvoker.postProcess(withParams, null, null));

        // then
        assertThat(ex.getMessage(), containsString("@PostConstruct method may not be static or have any parameters"));
    }

    @Test
    public void shouldThrowForStaticPostConstructMethod() {
        // given
        Static classWithStaticMethod = new Static();

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> postConstructInvoker.postProcess(classWithStaticMethod, null, null));

        // then
        assertThat(ex.getMessage(), containsString("@PostConstruct method may not be static or have any parameters"));
    }

    @Test
    public void shouldForwardExceptionFromPostConstruct() {
        // given
        ThrowsException throwsException = new ThrowsException();

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> postConstructInvoker.postProcess(throwsException, null, null));

        // then
        assertThat(ex.getMessage(), containsString("Could not invoke method"));
    }

    @Test
    public void shouldThrowForMultiplePostConstructMethods() {
        // given
        MultiplePostConstructs multiplePostConstructs =
            new MultiplePostConstructs();

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> postConstructInvoker.postProcess(multiplePostConstructs, null, null));

        // then
        assertThat(ex.getMessage(), containsString("Multiple methods with @PostConstruct"));
    }

    @Test
    public void shouldThrowForPostConstructNotReturningVoid() {
        // given
        NotVoidReturnType notVoidReturnType = new NotVoidReturnType();

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> postConstructInvoker.postProcess(notVoidReturnType, null, null));

        // then
        assertThat(ex.getMessage(), containsString("@PostConstruct method must have return type void"));
    }

    @Test
    public void shouldCallPostConstructOnParentsButNotOnNoMethodScanClasses() {
        // given
        ChildClass childClass = new ChildClass();

        // when
        postConstructInvoker.postProcess(childClass, null, null);

        // then
        assertThat(childClass.wasChildPostConstCalled, equalTo(true));
        assertThat(childClass.wasParentPostConstCalled, equalTo(true));
    }
    
    
    // ---------------
    // Test classes
    // ---------------
    private static final class WithParams {
        @PostConstruct
        public void invalidPostConstr(BetaManager betaManager) {
        }
    }

    private static final class Static {
        @PostConstruct
        public static void invalidMethod() {
            // --
        }
    }

    private static final class ThrowsException {
        @PostConstruct
        public void throwingPostConstruct() {
            throw new IllegalStateException("Exception in post construct");
        }
    }

    private static final class NotVoidReturnType {
        @PostConstruct
        public int returnsInt() {
            return 42;
        }
    }

    private static final class MultiplePostConstructs {
        @PostConstruct
        public void postConstruct1() {
            // --
        }
        @PostConstruct
        public void postConstruct2() {
            // --
        }
    }

    // ---------
    // Inheritance test classes
    // ---------
    private static class ParentClass {
        boolean wasParentPostConstCalled = false;

        @PostConstruct
        private void postConstruct() {
            wasParentPostConstCalled = true;
        }
    }

    @NoMethodScan
    private static class MiddleClass extends ParentClass {
        @PostConstruct
        private void postConstruct() {
            throw new IllegalStateException("Should not be called");
        }
    }

    private static final class ChildClass extends MiddleClass {
        boolean wasChildPostConstCalled = false;

        @PostConstruct
        private void postConstruct() {
            wasChildPostConstCalled = true;
        }
    }
}