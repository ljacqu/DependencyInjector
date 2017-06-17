package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BadFieldInjection;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithAnnotations;
import ch.jalu.injector.samples.ClassWithInjectMethod;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.FieldInjectionWithAnnotations;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InjectOnDifferentMembersClass;
import ch.jalu.injector.samples.InstantiationFallbackClasses.ClassWithPostConstruct;
import ch.jalu.injector.samples.InstantiationFallbackClasses.FallbackClass;
import ch.jalu.injector.samples.InstantiationFallbackClasses.InvalidNoArgConstructorClass;
import ch.jalu.injector.samples.InvalidClass;
import ch.jalu.injector.samples.InvalidFinalInjectField;
import ch.jalu.injector.samples.InvalidMultipleInjectConstructors;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Size;
import ch.jalu.injector.samples.StaticFieldInjection;
import ch.jalu.injector.samples.inheritance.Child;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static ch.jalu.injector.TestUtils.annotationOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link StandardInjectionProvider} and {@link StandardInjection}.
 */
public class StandardInjectionTest {
    
    private StandardInjectionProvider provider = new StandardInjectionProvider();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Test
    public void shouldReturnDependencies() {
        // given
        Instantiation<ClassWithAnnotations> injection = provider.safeGet(ClassWithAnnotations.class);

        // when
        List<ObjectIdentifier> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(3));
        assertDependencyEqualTo(dependencies.get(0), int.class, Size.class);
        assertDependencyEqualTo(dependencies.get(1), GammaService.class);
        assertDependencyEqualTo(dependencies.get(2), long.class, Duration.class);
        assertThat(((Size) dependencies.get(0).getAnnotations().get(0)).value(), equalTo("box"));
    }

    @Test
    public void shouldReturnDependenciesForFieldInjection() {
        // given
        Instantiation<FieldInjectionWithAnnotations> injection =
            provider.safeGet(FieldInjectionWithAnnotations.class);

        // when
        List<ObjectIdentifier> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(4));
        assertDependencyEqualTo(dependencies.get(0), BetaManager.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(1), int.class, Inject.class, Size.class);
        assertDependencyEqualTo(dependencies.get(2), long.class, Duration.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(3), ClassWithAnnotations.class, Inject.class);

        assertThat(((Size) dependencies.get(1).getAnnotations().get(1)).value(), equalTo("chest"));
    }

    @Test
    public void shouldInstantiate() {
        // given
        GammaService gammaService = new GammaService(
            AlphaService.newInstance(new ProvidedClass("")));
        Instantiation<ClassWithAnnotations> injection = provider.safeGet(ClassWithAnnotations.class);

        // when
        ClassWithAnnotations instance = injection.instantiateWith(-112, gammaService, 19L);

        // then
        assertThat(instance, not(nullValue()));
        assertThat(instance.getSize(), equalTo(-112));
        assertThat(instance.getGammaService(), equalTo(gammaService));
        assertThat(instance.getDuration(), equalTo(19L));
    }

    @Test
    public void shouldInstantiateClassWithConstructorInjection() {
        // given
        Instantiation<GammaService> instantiation = provider.safeGet(GammaService.class);

        // when
        GammaService gammaService = instantiation.instantiateWith(AlphaService.newInstance(new ProvidedClass("")));

        // then
        assertThat(gammaService, not(nullValue()));
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForNullValue() {
        // given
        Instantiation<ClassWithAnnotations> injection = provider.safeGet(ClassWithAnnotations.class);

        // when / then
        injection.instantiateWith(-112, null, 12L);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowUponInstantiationError() {
        // given
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        Instantiation<InvalidClass> injection = provider.safeGet(InvalidClass.class);

        // when
        injection.instantiateWith(alphaService, 5);
    }

    @Test
    public void shouldSupportMixedInjectionTypes() {
        // given
        Instantiation<InjectOnDifferentMembersClass> injection = provider.safeGet(InjectOnDifferentMembersClass.class);

        // when
        List<ObjectIdentifier> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(3));
        assertDependencyEqualTo(dependencies.get(0), ProvidedClass.class);
        assertDependencyEqualTo(dependencies.get(1), GammaService.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(2), BetaManager.class, Inject.class);
    }

    @Test
    public void shouldInstantiateClass() {
        // given
        Instantiation<BetaManager> injection = provider.safeGet(BetaManager.class);
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);

        // when
        BetaManager betaManager = injection.instantiateWith(providedClass, gammaService, alphaService);

        // then
        assertThat(betaManager, not(nullValue()));
        assertThat(betaManager.getDependencies(), arrayContaining(providedClass, gammaService, alphaService));
    }

    @Test
    public void shouldThrowForNoSuitableConstructor() {
        // given / when
        Instantiation<BadFieldInjection> injection = provider.safeGet(BadFieldInjection.class);

        // then
        assertThat(injection, nullValue());
    }

    @Test
    public void shouldForwardExceptionDuringInstantiation() {
        // given
        Instantiation<ThrowingConstructor> injection = provider.safeGet(ThrowingConstructor.class);

        // when / then
        try {
            injection.instantiateWith(new ProvidedClass(""));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            assertThat(e.getCause(), instanceOf(InvocationTargetException.class));
            assertThat(e.getCause().getCause().getMessage(), equalTo("Exception in constructor"));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForInvalidFieldValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);
        Instantiation<BetaManager> injection = provider.safeGet(BetaManager.class);

        // when / then
        // Correct order is provided, gamma, alpha
        injection.instantiateWith(providedClass, alphaService, gammaService);
    }

    @Test
    public void shouldSupportInjectionOnStaticFields() {
        // given
        Instantiation<StaticFieldInjection> injection = provider.safeGet(StaticFieldInjection.class);

        // when / then
        assertThat(injection.getDependencies(), hasSize(2));
        assertDependencyEqualTo(injection.getDependencies().get(0), ProvidedClass.class, Inject.class);
        assertDependencyEqualTo(injection.getDependencies().get(1), AlphaService.class, Inject.class);
    }

    @Test
    public void shouldAllowPrivateDefaultConstructorForPrivateInnerClass() {
        // given / when
        Instantiation<InnerClassWithPrivateConstructor> injection = provider.safeGet(InnerClassWithPrivateConstructor.class);

        // then
        assertThat(injection, not(nullValue()));
        assertThat(injection.getDependencies(), empty());
    }

    @Test
    public void shouldNotFindFieldDependencies() {
        // given / when
        Instantiation<NoFieldScanClass> injection = provider.safeGet(NoFieldScanClass.class);

        // then
        assertThat(injection.getDependencies(), empty());
    }

    @Test
    public void shouldInstantiateClassWithNoInjectAnnotation() {
        // given
        Instantiation<FallbackClass> instantiation = provider.safeGet(FallbackClass.class);

        // when
        FallbackClass result = instantiation.instantiateWith();

        // then
        assertThat(result, not(nullValue()));
    }

    @Test
    public void shouldHaveEmptyDependenciesAndAnnotations() {
        // given
        Instantiation<FallbackClass> instantiation =
            provider.safeGet(FallbackClass.class);

        // when
        List<ObjectIdentifier> dependencies = instantiation.getDependencies();

        // then
        assertThat(dependencies, empty());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowIfArgumentsAreSupplied() {
        // given
        Instantiation<FallbackClass> instantiation =
            provider.safeGet(FallbackClass.class);

        // when / then
        instantiation.instantiateWith("some argument");
    }

    @Test
    public void shouldReturnNullForMissingNoArgsConstructor() {
        // given / when / then
        assertThat(provider.safeGet(InvalidNoArgConstructorClass.class), nullValue());
    }

    @Test
    public void shouldInstantiateNoDependencyClass() {
        // given
        Instantiation<ClassWithPostConstruct> instantiation = provider.safeGet(ClassWithPostConstruct.class);

        // when
        ClassWithPostConstruct instance = instantiation.instantiateWith();

        // then
        assertThat(instance, not(nullValue()));
    }

    @Test
    public void shouldHandleInheritance() {
        // given / when
        Instantiation<Child> instantiation = provider.safeGet(Child.class);

        // then
        assertThat(instantiation.getDependencies(), hasSize(5));
    }

    @Test
    public void shouldThrowDueToFinalField() {
        // expect
        exceptionCatcher.expect("may not be final and have @Inject");

        // when
        provider.safeGet(InvalidFinalInjectField.class);
    }

    @Test
    public void shouldThrowForMultipleInjectConstructors() {
        // expect
        exceptionCatcher.expect("may not have multiple @Inject constructors");

        // when
        provider.safeGet(InvalidMultipleInjectConstructors.class);
    }

    @Test
    public void shouldThrowForClassWithInjectMethods() {
        // expect
        exceptionCatcher.expect("@Inject on methods is not supported");

        // when
        provider.safeGet(ClassWithInjectMethod.class);
    }

    @SafeVarargs
    private static void assertDependencyEqualTo(ObjectIdentifier dependency, Class<?> type,
                                                Class<? extends Annotation>... annotations) {
        assertThat(dependency.getType(), equalTo(type));
        assertThat(dependency.getAnnotations(), hasSize(annotations.length));

        for (int i = 0; i < annotations.length; ++i) {
            assertThat(dependency.getAnnotations().get(i), annotationOf(annotations[i]));
        }
    }

    private static final class ThrowingConstructor {
        @Inject
        private ProvidedClass providedClass;

        public ThrowingConstructor() {
            throw new UnsupportedOperationException("Exception in constructor");
        }
    }

    private static final class InnerClassWithPrivateConstructor {
        // If the class is private, the constructor may be private as well
        private InnerClassWithPrivateConstructor() {
        }
    }

    @NoFieldScan
    private static final class NoFieldScanClass {
        @Inject
        private ProvidedClass providedClass;
    }
}
