package ch.jalu.injector;

import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;
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
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link InjectorImpl}.
 */
public class InjectorImplTest {

    private final String ALLOWED_PACKAGE = getClass().getPackage().getName() + ".samples";


    private Injector injector;
    private InjectorConfig config;
    private SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();

    // As we test many cases that throw exceptions, we use JUnit's ExpectedException Rule
    // to make sure that we receive the exception we expect
    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Before
    public void initialize() throws NoSuchFieldException {
        injector = new InjectorBuilder()
            .addHandlers(savedAnnotationsHandler)
            .addDefaultHandlers(ALLOWED_PACKAGE)
            .create();

        // Since this is a test for the concrete implementation, a quick check to make sure we get the class we expect
        // Should the implementation ever change in the future, we'll be alerted that this test needs updating
        if (!(injector instanceof InjectorImpl)) {
            throw new IllegalStateException("Injector from builder is not of type InjectorImpl");
        }

        config = (InjectorConfig) ReflectionUtils
            .getFieldValue(injector.getClass().getDeclaredField("config"), injector);
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
        exceptionCatcher.expect("outside of the allowed packages", Integer.class);
        injector.getSingleton(InvalidClass.class);
    }

    @Test
    public void shouldThrowForUnregisteredPrimitiveType() {
        // given / when / then
        exceptionCatcher.expect("Primitive types must be provided", int.class);
        injector.getSingleton(int.class);
    }

    @Test
    public void shouldPassValueByAnnotation() {
        // given
        int size = 12;
        long duration = -15482L;
        savedAnnotationsHandler.register(Size.class, size);
        savedAnnotationsHandler.register(Duration.class, duration);

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
        exceptionCatcher.expect("Found cyclic dependency", CircularClasses.Circular3.class);
        injector.getSingleton(CircularClasses.Circular3.class);
    }

    @Test
    public void shouldThrowForFieldInjectionWithoutNoArgsConstructor() {
        // given / when / then
        exceptionCatcher.expect("Did not find instantiation method", BadFieldInjection.class);
        injector.getSingleton(BadFieldInjection.class);
    }

    @Test
    public void shouldInjectFieldsWithAnnotationsProperly() {
        // given
        savedAnnotationsHandler.register(Size.class, 2809375);
        savedAnnotationsHandler.register(Duration.class, 13095L);

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
        exceptionCatcher.expect("There is already an object present", ProvidedClass.class);
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldThrowForRegisterWithNull() {
        // given / when / then
        exceptionCatcher.expect("may not be null", String.class);
        injector.register(String.class, null);
    }

    @Test
    public void shouldThrowForAbstractNonRegisteredDependency() {
        // given / when / then
        exceptionCatcher.expect("cannot be instantiated", ClassWithAbstractDependency.AbstractDependency.class);
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
        exceptionCatcher.expect("There is already an object present", BetaManager.class);
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
        exceptionCatcher.expect("is static but annotated with @Inject", InvalidStaticFieldInjection.class);
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
        Collection<Reloadable> children1 = injector.retrieveAll(Reloadable.class);
        Collection<CircularClasses> children2 = injector.retrieveAll(CircularClasses.class);
        Collection<Object> children3 = injector.retrieveAll(Object.class);

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
