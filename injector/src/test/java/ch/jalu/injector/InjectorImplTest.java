package ch.jalu.injector;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.dependency.providers.Delta;
import ch.jalu.injector.handlers.dependency.providers.Delta1Provider;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.SimpleResolution;
import ch.jalu.injector.handlers.instantiation.StandardInjectionProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BadFieldInjection;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.CircularClasses;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.samples.ClassWithAnnotations;
import ch.jalu.injector.samples.Duration;
import ch.jalu.injector.samples.FieldInjectionWithAnnotations;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.InstantiationFallbackClasses;
import ch.jalu.injector.samples.InvalidClass;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Reloadable;
import ch.jalu.injector.samples.SampleInstantiationImpl;
import ch.jalu.injector.samples.Size;
import ch.jalu.injector.samples.StaticFieldInjection;
import ch.jalu.injector.samples.inheritance.Child;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link InjectorImpl}.
 */
public class InjectorImplTest {

    private final String ALLOWED_PACKAGE = getClass().getPackage().getName() + ".samples";

    private Injector injector;
    private InjectorConfig config;

    @BeforeEach
    public void initialize() {
        injector = new InjectorBuilder()
            .addDefaultHandlers(ALLOWED_PACKAGE)
            .create();

        // Since this is a test for the concrete implementation, a quick check to make sure we get the class we expect
        // Should the implementation ever change in the future, we'll be alerted that this test needs updating
        if (!(injector instanceof InjectorImpl)) {
            throw new IllegalStateException("Injector from builder is not of type InjectorImpl");
        }

        config = ((InjectorImpl) injector).getConfig();
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldInitializeElements() {
        // given / when
        BetaManager betaManager = injector.getSingleton(BetaManager.class);

        // then
        assertThat(betaManager, not(nullValue()));
        for (Object o : betaManager.getDependencies()) {
            assertThat(o, not(nullValue()));
        }
    }

    @Test
    public void shouldThrowForInvalidPackage() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(InvalidClass.class));

        // then
        assertThat(ex.getMessage(), containsString("outside of the allowed packages"));
    }

    @Test
    public void shouldThrowForUnregisteredPrimitiveType() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(int.class));

        // then
        assertThat(ex.getMessage(), containsString("Primitive types must be provided"));
    }

    @Test
    public void shouldPassValueByAnnotation() {
        // given
        int size = 12;
        long duration = -15482L;
        injector.provide(Size.class, size);
        injector.provide(Duration.class, duration);

        // when
        ClassWithAnnotations object = injector.getSingleton(ClassWithAnnotations.class);

        // then
        assertThat(object, not(nullValue()));
        assertThat(object.getSize(), equalTo(size));
        assertThat(object.getDuration(), equalTo(duration));
        // some sample check to make sure we only have one instance of GammaService
        assertThat(object.getGammaService(), equalTo(injector.getSingleton(BetaManager.class).getDependencies()[1]));
    }

    @Test
    public void shouldRecognizeCircularReferences() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(CircularClasses.Circular3.class));

        // then
        assertThat(ex.getMessage(), containsString("Found cyclic dependency"));
    }

    @Test
    public void shouldThrowForFieldInjectionWithoutNoArgsConstructor() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(BadFieldInjection.class));

        // then
        assertThat(ex.getMessage(), containsString("Did not find instantiation method"));
    }

    @Test
    public void shouldInjectFieldsWithAnnotationsProperly() {
        // given
        injector.provide(Size.class, 2809375);
        injector.provide(Duration.class, 13095L);

        // when
        FieldInjectionWithAnnotations result = injector.getSingleton(FieldInjectionWithAnnotations.class);

        // then
        assertThat(result.getSize(), equalTo(2809375));
        assertThat(result.getDuration(), equalTo(13095L));
        assertThat(result.getBetaManager(), not(nullValue()));
        assertThat(result.getClassWithAnnotations(), not(nullValue()));
        assertThat(result.getClassWithAnnotations().getGammaService(),
            equalTo(result.getBetaManager().getDependencies()[1]));
    }

    @Test
    public void shouldThrowForSecondRegistration() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(BadFieldInjection.class));

        // then
        assertThat(ex.getMessage(), containsString("Did not find instantiation method"));
    }

    @Test
    public void shouldThrowForRegisterWithNull() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.register(String.class, null));

        // then
        assertThat(ex.getMessage(), containsString("may not be null"));
    }

    @Test
    public void shouldThrowForAbstractNonRegisteredDependency() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(ClassWithAbstractDependency.class));

        // then
        assertThat(ex.getMessage(), containsString("cannot be instantiated"));
    }

    @Test
    public void shouldInstantiateWithImplementationOfAbstractDependency() {
        // given
        ClassWithAbstractDependency.ConcreteDependency concrete = new ClassWithAbstractDependency.ConcreteDependency();
        injector.register(ClassWithAbstractDependency.AbstractDependency.class, concrete);

        // when
        ClassWithAbstractDependency cwad = injector.getSingleton(ClassWithAbstractDependency.class);

        // then
        assertThat(cwad.getAbstractDependency() == concrete, equalTo(true));
        assertThat(cwad.getAlphaService(), not(nullValue()));
    }

    @Test
    public void shouldThrowForAlreadyRegisteredClass() {
        // given
        injector.register(BetaManager.class, new BetaManager());

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.register(BetaManager.class, new BetaManager()));

        // then
        assertThat(ex.getMessage(), containsString("There is already an object present"));
    }

    @Test
    public void shouldCreateNewUntrackedInstance() {
        // given / when
        AlphaService singletonScoped = injector.getSingleton(AlphaService.class);
        AlphaService requestScoped = injector.newInstance(AlphaService.class);

        // then
        assertThat(singletonScoped.getProvidedClass(), not(nullValue()));
        assertThat(singletonScoped.getProvidedClass(), equalTo(requestScoped.getProvidedClass()));
        assertThat(singletonScoped, not(sameInstance(requestScoped)));
    }

    @Test
    public void shouldThrowForStaticFieldToInject() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.newInstance(StaticFieldInjection.class));

        // then
        assertThat(ex.getMessage(), containsString("@Inject may not be placed on static fields"));
    }

    @Test
    public void shouldFallbackToSimpleInstantiationForPlainClass() {
        // given / when
        InstantiationFallbackClasses.HasFallbackDependency result =
            injector.getSingleton(InstantiationFallbackClasses.HasFallbackDependency.class);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getGammaService(), not(nullValue()));
        assertThat(result.getFallbackDependency(), not(nullValue()));
    }

    @Test
    public void shouldRetrieveExistingInstancesOnly() {
        // given
        injector.getSingleton(GammaService.class);

        // when
        AlphaService alphaService = injector.getIfAvailable(AlphaService.class);
        BetaManager betaManager = injector.getIfAvailable(BetaManager.class);

        // then
        // was initialized because is dependency of GammaService
        assertThat(alphaService, not(nullValue()));
        // nothing caused this to be initialized
        assertThat(betaManager, nullValue());
    }

    @Test
    public void shouldGetAllSingletonsOfGivenType() {
        // given
        // trigger singleton registration
        injector.getSingleton(AlphaService.class);
        injector.getSingleton(BetaManager.class);
        injector.getSingleton(GammaService.class);

        // when
        Collection<Reloadable> children1 = injector.retrieveAllOfType(Reloadable.class);
        Collection<CircularClasses> children2 = injector.retrieveAllOfType(CircularClasses.class);
        Collection<Object> children3 = injector.retrieveAllOfType(Object.class);

        // then
        assertThat(children1, hasSize(2));
        assertThat(children2, empty());
        assertThat(children3, hasSize(5)); // Alpha, Beta, Gamma + ProvidedClass + Injector
    }

    @Test
    public void shouldUseCustomInstantiation() {
        // given
        List<Handler> handlers = new ArrayList<>(config.getHandlers());
        // hack: the inline class is not within the allowed packages, so clear the package validator for this test
        handlers.removeIf(handler -> handler instanceof DefaultInjectionProvider);
        handlers.add(new StandardInjectionProvider());
        handlers.add(new SampleInstantiationImpl());
        config.getHandlers().clear();
        config.getHandlers().addAll(handlers);

        // when
        CustomInstantiationExample example = injector.getSingleton(CustomInstantiationExample.class);

        // then
        assertThat(example, not(nullValue()));
    }

    @Test
    public void shouldForwardToAnnotationValueHandlers() throws Exception {
        // given
        Handler annoValHandler1 = mock(Handler.class);
        Handler annoValHandler2 = mock(Handler.class);
        config.addHandlers(Arrays.asList(annoValHandler1, annoValHandler2));
        Object object = new Object();

        // when
        injector.provide(Duration.class, object);

        // then
        verify(annoValHandler1).onAnnotation(Duration.class, object);
        verify(annoValHandler2).onAnnotation(Duration.class, object);
    }

    @Test
    public void shouldThrowForNullAsAnnotation() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.provide(null, new Object()));

        // then
        assertThat(ex.getMessage(), containsString("annotation may not be null"));
    }

    @Test
    public void shouldForwardException() throws Exception {
        // given
        Class<? extends Annotation> annotation = Size.class;
        Object object = 123;
        Handler annoValHandler = mock(Handler.class);
        doThrow(Exception.class).when(annoValHandler).onAnnotation(annotation, object);
        config.addHandlers(Collections.singletonList(annoValHandler));

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.provide(annotation, object));

        // then
        assertThat(ex.getMessage(), containsString("An error occurred"));
    }

    @Test
    public void shouldThrowForNullProvider() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.registerProvider(Delta.class, (Provider<Delta>) null));

        // then
        assertThat(ex.getMessage(), containsString("may not be null"));
    }

    @Test
    public void shouldThrowForNullClassWithProvider() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.registerProvider(null, mock(Provider.class)));

        // then
        assertThat(ex.getMessage(), containsString("may not be null"));
    }

    @Test
    public void shouldThrowForNullProviderClass() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.registerProvider(BetaManager.class, (Class<Provider<BetaManager>>) null));

        // then
        assertThat(ex.getMessage(), containsString("may not be null"));
    }

    @Test
    public void shouldThrowForNullClassWithProviderClass() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.registerProvider(null, Provider.class));

        // then
        assertThat(ex.getMessage(), containsString("may not be null"));
    }

    @Test
    public void shouldInstantiateForAllAvailableDependencies() {
        // given
        // Trigger initialization of AlphaService
        injector.getSingleton(AlphaService.class);

        // when
        GammaService gammaService = injector.createIfHasDependencies(GammaService.class);

        // then
        assertThat(gammaService, not(nullValue()));
        assertThat(injector.getIfAvailable(GammaService.class), nullValue());
    }

    @Test
    public void shouldNotInstantiateForMissingDependencies() {
        // given / when
        GammaService gammaService = injector.createIfHasDependencies(GammaService.class);

        // then
        assertThat(gammaService, nullValue());
    }

    @Test
    public void shouldInstantiateForAllAvailableDependenciesAndAnnotations() {
        // given
        injector.provide(Size.class, 2809375);
        injector.provide(Duration.class, 13095L);
        injector.register(BetaManager.class, new BetaManager());
        injector.getSingleton(ClassWithAnnotations.class); // trigger instantiation

        // when
        FieldInjectionWithAnnotations result = injector.createIfHasDependencies(FieldInjectionWithAnnotations.class);

        // then
        assertThat(result, not(nullValue()));
    }

    @Test
    public void shouldReturnNullForMissingDependency() {
        // given
        injector.provide(Size.class, 2809375);
        injector.provide(Duration.class, 13095L);
        injector.register(BetaManager.class, new BetaManager());

        // when
        FieldInjectionWithAnnotations result = injector.createIfHasDependencies(FieldInjectionWithAnnotations.class);

        // then
        assertThat(result, nullValue());
    }

    @Test
    public void shouldInstantiateClassWithInheritedInjects() {
        // given / when
        Child child = injector.getSingleton(Child.class);

        // then
        assertThat(child.isPostConstructRunAfterParent(), equalTo(true));
        assertThat(child.getChildGammaService(), not(nullValue()));
        assertThat(child.getParentProvidedClass(), not(nullValue()));
        assertThat(child.getParentBetaManager(), not(nullValue()));
        assertThat(child.getParentBetaManager(), sameInstance(child.getBetaManager()));
        assertThat(child.getAlphaService(), not(nullValue()));
    }

    @Test
    public void shouldRunPostConstructHandlersOnNewlyCreatedObjects() {
        // given
        injector.provide(Duration.class, 3);
        injector.provide(Size.class, 3);
        config.getHandlers().removeIf(h -> h instanceof PostConstructMethodInvoker);
        PostConstructMethodInvoker postConstructInvoker = Mockito.spy(new PostConstructMethodInvoker());
        config.getHandlers().add(postConstructInvoker);

        // when
        injector.getSingleton(Child.class); // Child, GammaService, BetaManager, AlphaService
        injector.getSingleton(FieldInjectionWithAnnotations.class); // this class + ClassWithAnnotations
        injector.getSingleton(GammaService.class);

        // then
        ArgumentCaptor<Object> argCaptor = ArgumentCaptor.forClass(Object.class);
        verify(postConstructInvoker, times(6)).postProcess(argCaptor.capture(), any(ResolutionContext.class), any(Resolution.class));
        assertThat(argCaptor.getAllValues(), containsInAnyOrder(
            instanceOf(GammaService.class), instanceOf(BetaManager.class), instanceOf(AlphaService.class), instanceOf(Child.class),
            instanceOf(FieldInjectionWithAnnotations.class), instanceOf(ClassWithAnnotations.class)));
    }

    @Test
    public void shouldRunPostConstructOnNewInstances() {
        // given
        config.getHandlers().removeIf(h -> h instanceof PostConstructMethodInvoker);
        PostConstructMethodInvoker postConstructInvoker = Mockito.spy(new PostConstructMethodInvoker());
        config.getHandlers().add(postConstructInvoker);

        // when
        AlphaService s1 = injector.getSingleton(AlphaService.class); // singleton first time: counts
        AlphaService s2 = injector.getSingleton(AlphaService.class);
        AlphaService i1 = injector.newInstance(AlphaService.class);  // new instance: counts
        AlphaService s3 = injector.getSingleton(AlphaService.class);
        AlphaService i2 = injector.newInstance(AlphaService.class);  // new instance: counts
        AlphaService i3 = injector.newInstance(AlphaService.class);  // new instance: counts
        AlphaService i4 = injector.createIfHasDependencies(AlphaService.class); // counts
        AlphaService s4 = injector.getIfAvailable(AlphaService.class);
        Collection<AlphaService> singletons = injector.retrieveAllOfType(AlphaService.class);

        // then
        verify(postConstructInvoker, times(5)).postProcess(any(Object.class), any(ResolutionContext.class), any(Resolution.class));
        assertAreAllSameInstance(s1, s2, s3, s4);
        assertAreAllDifferentInstances(s1, i1, i2, i3, i4);
        assertThat(singletons, contains(sameInstance(s1)));
    }

    @Test
    public void shouldForwardExceptionFromProviderHandler() throws Exception {
        // given
        Handler handler = mock(Handler.class);
        doThrow(IllegalStateException.class).when(handler).onProvider(any(Class.class), any(Provider.class));
        config.getHandlers().add(handler);

        try {
            // when
            injector.registerProvider(Delta.class, new Delta1Provider());
            fail("Expected exception");
        } catch (InjectorException e) {
            // then
            assertThat(e.getMessage(), containsString("An error occurred"));
            assertThat(e.getCause(), instanceOf(IllegalStateException.class));
        }
    }

    @Test
    public void shouldForwardExceptionFromProviderClassHandler() throws Exception {
        // given
        Handler handler = mock(Handler.class);
        doThrow(UnsupportedOperationException.class).when(handler).onProviderClass(any(Class.class), any(Class.class));
        config.getHandlers().add(handler);

        try {
            // when
            injector.registerProvider(Delta.class, Delta1Provider.class);
            fail("Expected exception");
        } catch (InjectorException e) {
            // then
            assertThat(e.getMessage(), containsString("An error occurred"));
            assertThat(e.getCause(), instanceOf(UnsupportedOperationException.class));
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldThrowForNullReturnedAsDependency() throws Exception {
        // given
        Handler handler = mock(Handler.class);
        given(handler.resolve(any(ResolutionContext.class))).willAnswer(
            invocation -> ((ResolutionContext) invocation.getArgument(0)).getIdentifier().getTypeAsClass() == AlphaService.class
                ? new SimpleResolution<>(null)
                : null
        );
        config.getHandlers().add(1, handler);

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> injector.getSingleton(Child.class));

        // then
        assertThat(ex.getMessage(), containsString("Found null returned as dependency"));
    }

    private static void assertAreAllSameInstance(Object... objects) {
        assertThat(Stream.of(objects).map(System::identityHashCode).distinct().count(), equalTo(1L));
    }

    private static void assertAreAllDifferentInstances(Object... objects) {
        assertThat(Stream.of(objects).map(System::identityHashCode).distinct().count(), equalTo((long) objects.length));
    }

    /**
     * Matches {@link SampleInstantiationImpl}.
     */
    private static final class CustomInstantiationExample {
        private CustomInstantiationExample() {
            // private constructor = not eligible for instantiation fallback
        }

        public static CustomInstantiationExample create() {
            return new CustomInstantiationExample();
        }
    }
}
