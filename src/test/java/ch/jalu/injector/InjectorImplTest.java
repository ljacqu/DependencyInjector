package ch.jalu.injector;

import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.annotationvalues.AnnotationValueHandler;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.provider.impl.Delta;
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
import ch.jalu.injector.samples.InvalidStaticFieldInjection;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Reloadable;
import ch.jalu.injector.samples.SampleInstantiationImpl;
import ch.jalu.injector.samples.Size;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Test for {@link InjectorImpl}.
 */
public class InjectorImplTest {

    private final String ALLOWED_PACKAGE = getClass().getPackage().getName() + ".samples";

    private Injector injector;
    private InjectorConfig config;

    // As we test many cases that throw exceptions, we use JUnit's ExpectedException Rule
    // to make sure that we receive the exception we expect
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Before
    public void initialize() throws NoSuchFieldException {
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
        // given / when / then
        exceptionCatcher.expect("outside of the allowed packages");
        injector.getSingleton(InvalidClass.class);
    }

    @Test
    public void shouldThrowForUnregisteredPrimitiveType() {
        // given / when / then
        exceptionCatcher.expect("Primitive types must be provided");
        injector.getSingleton(int.class);
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
        // given / when / then
        exceptionCatcher.expect("Found cyclic dependency");
        injector.getSingleton(CircularClasses.Circular3.class);
    }

    @Test
    public void shouldThrowForFieldInjectionWithoutNoArgsConstructor() {
        // given / when / then
        exceptionCatcher.expect("Did not find instantiation method");
        injector.getSingleton(BadFieldInjection.class);
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
        // given / when / then
        exceptionCatcher.expect("There is already an object present");
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldThrowForRegisterWithNull() {
        // given / when / then
        exceptionCatcher.expect("may not be null");
        injector.register(String.class, null);
    }

    @Test
    public void shouldThrowForAbstractNonRegisteredDependency() {
        // given / when / then
        exceptionCatcher.expect("cannot be instantiated");
        injector.getSingleton(ClassWithAbstractDependency.class);
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

        // when / then
        exceptionCatcher.expect("There is already an object present");
        injector.register(BetaManager.class, new BetaManager());
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
    public void shouldThrowForStaticFieldInjection() {
        // given / when / then
        exceptionCatcher.expect("is static but annotated with @Inject");
        injector.newInstance(InvalidStaticFieldInjection.class);
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
        // hack: the inline class is not within the allowed packages, so clear the package validator for this test
        config.getPreConstructHandlers().clear();
        config.addInstantiationProviders(Collections.singletonList(new SampleInstantiationImpl()));

        // when
        CustomInstantiationExample example = injector.getSingleton(CustomInstantiationExample.class);

        // then
        assertThat(example, not(nullValue()));
    }

    @Test
    public void shouldThrowIfNoInstantiationMethodsAreAvailable() {
        // given
        List<Handler> handlers = getAllHandlersExceptInstantiationProviders();
        Injector injector = new InjectorBuilder().addHandlers(handlers).create();

        // expect
        exceptionCatcher.expect("You did not register any instantiation methods");

        // when
        injector.getSingleton(CustomInstantiationExample.class);
    }

    @Test
    public void shouldForwardToAnnotationValueHandlers() throws Exception {
        // given
        AnnotationValueHandler annoValHandler1 = mock(AnnotationValueHandler.class);
        AnnotationValueHandler annoValHandler2 = mock(AnnotationValueHandler.class);
        config.addAnnotationValueHandlers(Arrays.asList(annoValHandler1, annoValHandler2));
        Object object = new Object();

        // when
        injector.provide(Duration.class, object);

        // then
        verify(annoValHandler1).processProvided(Duration.class, object);
        verify(annoValHandler2).processProvided(Duration.class, object);
    }

    @Test
    public void shouldThrowForNullAsAnnotation() {
        // given

        // expect
        exceptionCatcher.expect("annotation may not be null");

        // when
        injector.provide(null, new Object());
    }

    @Test
    public void shouldForwardException() throws Exception {
        // given
        Class<? extends Annotation> annotation = Size.class;
        Object object = 123;
        AnnotationValueHandler annoValHandler = mock(AnnotationValueHandler.class);
        doThrow(Exception.class).when(annoValHandler).processProvided(annotation, object);
        config.addAnnotationValueHandlers(Collections.singletonList(annoValHandler));

        // expect
        exceptionCatcher.expect("An error occurred");

        // when
        injector.provide(annotation, object);
    }

    @Test
    public void shouldThrowForNullProvider() {
        // expect
        exceptionCatcher.expect("may not be null");

        // when
        injector.registerProvider(Delta.class, (Provider<Delta>) null);
    }

    @Test
    public void shouldThrowForNullClassWithProvider() {
        // expect
        exceptionCatcher.expect("may not be null");

        // when
        injector.registerProvider(null, mock(Provider.class));
    }

    @Test
    public void shouldThrowForNullProviderClass() {
        // expect
        exceptionCatcher.expect("may not be null");

        // when
        injector.registerProvider(BetaManager.class, (Class<Provider<BetaManager>>) null);
    }

    @Test
    public void shouldThrowForNullClassWithProviderClass() {
        // expect
        exceptionCatcher.expect("may not be null");

        // when
        injector.registerProvider(null, Provider.class);
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

    private static List<Handler> getAllHandlersExceptInstantiationProviders() {
        List<Handler> handlers = InjectorBuilder.createDefaultHandlers("");
        Iterator<Handler> iterator = handlers.iterator();
        while (iterator.hasNext()) {
            Handler next = iterator.next();
            if (next instanceof InstantiationProvider) {
                iterator.remove();
            }
        }
        return handlers;
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
