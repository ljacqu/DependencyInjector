package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.annotations.NoFieldScan;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.exceptions.InjectorReflectionException;
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
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static ch.jalu.injector.TestUtils.annotationOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link StandardInjectionProvider} and {@link StandardInjection}.
 */
class StandardInjectionTest {
    
    private StandardInjectionProvider provider = new StandardInjectionProvider();

    @Test
    void shouldReturnDependencies() {
        // given
        Resolution<ClassWithAnnotations> injection = provider.safeGet(ClassWithAnnotations.class);

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
    void shouldReturnDependenciesForFieldInjection() {
        // given
        Resolution<FieldInjectionWithAnnotations> injection =
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
    void shouldInstantiate() {
        // given
        GammaService gammaService = new GammaService(
            AlphaService.newInstance(new ProvidedClass("")));
        Resolution<ClassWithAnnotations> injection = provider.safeGet(ClassWithAnnotations.class);

        // when
        ClassWithAnnotations instance = injection.instantiateWith(-112, gammaService, 19L);

        // then
        assertThat(instance, not(nullValue()));
        assertThat(instance.getSize(), equalTo(-112));
        assertThat(instance.getGammaService(), equalTo(gammaService));
        assertThat(instance.getDuration(), equalTo(19L));
    }

    @Test
    void shouldInstantiateClassWithConstructorInjection() {
        // given
        Resolution<GammaService> instantiation = provider.safeGet(GammaService.class);

        // when
        GammaService gammaService = instantiation.instantiateWith(AlphaService.newInstance(new ProvidedClass("")));

        // then
        assertThat(gammaService, not(nullValue()));
    }

    @Test
    void shouldThrowUponInstantiationError() {
        // given
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        Resolution<InvalidClass> injection = provider.safeGet(InvalidClass.class);

        // when / then
        assertThrows(InjectorException.class,
            () -> injection.instantiateWith(alphaService, 5));
    }

    @Test
    void shouldSupportMixedInjectionTypes() {
        // given
        Resolution<InjectOnDifferentMembersClass> injection = provider.safeGet(InjectOnDifferentMembersClass.class);

        // when
        List<ObjectIdentifier> dependencies = injection.getDependencies();

        // then
        assertThat(dependencies, hasSize(3));
        assertDependencyEqualTo(dependencies.get(0), ProvidedClass.class);
        assertDependencyEqualTo(dependencies.get(1), GammaService.class, Inject.class);
        assertDependencyEqualTo(dependencies.get(2), BetaManager.class, Inject.class);
    }

    @Test
    void shouldInstantiateClass() {
        // given
        Resolution<BetaManager> injection = provider.safeGet(BetaManager.class);
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
    void shouldThrowForNoSuitableConstructor() {
        // given / when
        Resolution<BadFieldInjection> injection = provider.safeGet(BadFieldInjection.class);

        // then
        assertThat(injection, nullValue());
    }

    @Test
    void shouldForwardExceptionDuringInstantiation() {
        // given
        Resolution<ThrowingConstructor> injection = provider.safeGet(ThrowingConstructor.class);

        // when / then
        try {
            injection.instantiateWith(new ProvidedClass(""));
            fail("Expected exception to be thrown");
        } catch (InjectorException e) {
            assertThat(e.getCause(), instanceOf(InvocationTargetException.class));
            assertThat(e.getCause().getCause().getMessage(), equalTo("Exception in constructor"));
        }
    }

    @Test
    void shouldThrowForInvalidFieldValue() {
        // given
        ProvidedClass providedClass = new ProvidedClass("");
        AlphaService alphaService = AlphaService.newInstance(providedClass);
        GammaService gammaService = new GammaService(alphaService);
        Resolution<BetaManager> injection = provider.safeGet(BetaManager.class);

        // when / then
        // Correct order is provided, gamma, alpha
        assertThrows(InjectorReflectionException.class,
            () -> injection.instantiateWith(providedClass, alphaService, gammaService));
    }

    @Test
    void shouldSupportInjectionOnStaticFields() {
        // given
        Resolution<StaticFieldInjection> injection = provider.safeGet(StaticFieldInjection.class);

        // when / then
        assertThat(injection.getDependencies(), hasSize(2));
        assertDependencyEqualTo(injection.getDependencies().get(0), ProvidedClass.class, Inject.class);
        assertDependencyEqualTo(injection.getDependencies().get(1), AlphaService.class, Inject.class);
    }

    @Test
    void shouldAllowPrivateDefaultConstructorForPrivateInnerClass() {
        // given / when
        Resolution<InnerClassWithPrivateConstructor> injection = provider.safeGet(InnerClassWithPrivateConstructor.class);

        // then
        assertThat(injection, not(nullValue()));
        assertThat(injection.getDependencies(), empty());
    }

    @Test
    void shouldNotFindFieldDependencies() {
        // given / when
        Resolution<NoFieldScanClass> injection = provider.safeGet(NoFieldScanClass.class);

        // then
        assertThat(injection.getDependencies(), empty());
    }

    @Test
    void shouldInstantiateClassWithNoInjectAnnotation() {
        // given
        Resolution<FallbackClass> instantiation = provider.safeGet(FallbackClass.class);

        // when
        FallbackClass result = instantiation.instantiateWith();

        // then
        assertThat(result, not(nullValue()));
    }

    @Test
    void shouldHaveEmptyDependenciesAndAnnotations() {
        // given
        Resolution<FallbackClass> instantiation =
            provider.safeGet(FallbackClass.class);

        // when
        List<ObjectIdentifier> dependencies = instantiation.getDependencies();

        // then
        assertThat(dependencies, empty());
    }

    @Test
    void shouldThrowIfArgumentsAreSupplied() {
        // given
        Resolution<FallbackClass> instantiation =
            provider.safeGet(FallbackClass.class);

        // when / then
        assertThrows(InjectorException.class,
            () -> instantiation.instantiateWith("some argument"));
    }

    @Test
    void shouldReturnNullForMissingNoArgsConstructor() {
        // given / when / then
        assertThat(provider.safeGet(InvalidNoArgConstructorClass.class), nullValue());
    }

    @Test
    void shouldInstantiateNoDependencyClass() {
        // given
        Resolution<ClassWithPostConstruct> instantiation = provider.safeGet(ClassWithPostConstruct.class);

        // when
        ClassWithPostConstruct instance = instantiation.instantiateWith();

        // then
        assertThat(instance, not(nullValue()));
    }

    @Test
    void shouldHandleInheritance() {
        // given / when
        Resolution<Child> instantiation = provider.safeGet(Child.class);

        // then
        assertThat(instantiation.getDependencies(), hasSize(5));
    }

    @Test
    void shouldThrowDueToFinalField() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.safeGet(InvalidFinalInjectField.class));

        // then
        assertThat(ex.getMessage(), containsString("may not be final and have @Inject"));
    }

    @Test
    void shouldThrowForMultipleInjectConstructors() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.safeGet(InvalidMultipleInjectConstructors.class));

        // then
        assertThat(ex.getMessage(), containsString("may not have multiple @Inject constructors"));
    }

    @Test
    void shouldThrowForClassWithInjectMethods() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.safeGet(ClassWithInjectMethod.class));

        // then
        assertThat(ex.getMessage(), containsString("@Inject on methods is not supported"));
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

        ThrowingConstructor() {
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
