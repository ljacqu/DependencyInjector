package ch.jalu.injector;

import ch.jalu.injector.handlers.annotations.SavedAnnotationsHandler;
import ch.jalu.injector.exceptions.InjectorException;
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
import ch.jalu.injector.samples.InvalidPostConstruct;
import ch.jalu.injector.samples.InvalidStaticFieldInjection;
import ch.jalu.injector.samples.PostConstructTestClass;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.Reloadable;
import ch.jalu.injector.samples.Size;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.Matchers.containsString;
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

    private SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();
    private Injector injector;

    // As we test many cases that throw exceptions, we use JUnit's ExpectedException Rule
    // to make sure that we receive the exception we expect
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setInitializer() {
        InjectorConfig config = new InjectorConfig();
        config.setRootPackage(ALLOWED_PACKAGE);
        config.addAnnotationHandlers(Collections.singletonList(savedAnnotationsHandler));
        injector = new InjectorImpl(config);
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
        expectInjectorException("outside of the allowed packages", Integer.class);
        injector.getSingleton(InvalidClass.class);
    }

    @Test
    public void shouldThrowForUnregisteredPrimitiveType() {
        // given / when / then
        expectInjectorException("Primitive types must be provided", int.class);
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
        expectInjectorException("Found cyclic dependency", CircularClasses.Circular3.class);
        injector.getSingleton(CircularClasses.Circular3.class);
    }

    @Test
    public void shouldThrowForFieldInjectionWithoutNoArgsConstructor() {
        // given / when / then
        expectInjectorException("Did not find injection method", BadFieldInjection.class);
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
        expectInjectorException("There is already an object present", ProvidedClass.class);
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldThrowForRegisterWithNull() {
        // given / when / then
        expectInjectorException("may not be null", String.class);
        injector.register(String.class, null);
    }

    @Test
    public void shouldExecutePostConstructMethod() {
        // given
        savedAnnotationsHandler.register(Size.class, 15123);

        // when
        PostConstructTestClass testClass = injector.getSingleton(PostConstructTestClass.class);

        // then
        assertThat(testClass.wasPostConstructCalled(), equalTo(true));
        assertThat(testClass.getBetaManager(), not(nullValue()));
    }

    @Test
    public void shouldThrowForInvalidPostConstructMethod() {
        // given / when / then
        expectInjectorException("@PostConstruct method may not be static or have any parameters",
            InvalidPostConstruct.WithParams.class);
        injector.getSingleton(InvalidPostConstruct.WithParams.class);
    }

    @Test
    public void shouldThrowForStaticPostConstructMethod() {
        // given / when / then
        expectInjectorException("@PostConstruct method may not be static or have any parameters",
            InvalidPostConstruct.Static.class);
        injector.getSingleton(InvalidPostConstruct.Static.class);
    }

    @Test
    public void shouldForwardExceptionFromPostConstruct() {
        // given / when / then
        expectInjectorException("Could not invoke method", InvalidPostConstruct.ThrowsException.class);
        injector.getSingleton(InvalidPostConstruct.ThrowsException.class);
    }

    @Test
    public void shouldThrowForMultiplePostConstructMethods() {
        // given / when / then
        expectInjectorException("Multiple methods with @PostConstruct",
            InvalidPostConstruct.MultiplePostConstructs.class);
        injector.getSingleton(InvalidPostConstruct.MultiplePostConstructs.class);
    }

    @Test
    public void shouldThrowForPostConstructNotReturningVoid() {
        // given / when / then
        expectInjectorException("@PostConstruct method must have return type void",
            InvalidPostConstruct.NotVoidReturnType.class);
        injector.getSingleton(InvalidPostConstruct.NotVoidReturnType.class);
    }

    @Test
    public void shouldThrowForAbstractNonRegisteredDependency() {
        // given / when / then
        expectInjectorException("cannot be instantiated", ClassWithAbstractDependency.AbstractDependency.class);
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
        expectInjectorException("There is already an object present", BetaManager.class);
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
        expectInjectorException("is static but annotated with @Inject", InvalidStaticFieldInjection.class);
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

    private void expectInjectorException(String message, Class<?> concernedClass) {
        expectedException.expect(InjectorException.class);
        expectedException.expectMessage(containsString(message));
        expectedException.expect(hasClass(concernedClass));
    }

    private static <T extends InjectorException> Matcher<T> hasClass(final Class<?> clazz) {
        return new TypeSafeMatcher<T>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Expected exception with class '" + clazz.getSimpleName() + "'");
            }

            @Override
            public void describeMismatchSafely(T item, Description mismatchDescription) {
                String className = item.getClazz() == null ? "null" : item.getClazz().getSimpleName();
                mismatchDescription.appendText("had class '" + className + "'");
            }

            @Override
            protected boolean matchesSafely(T item) {
                // No need to make this null safe, the clazz should ALWAYS be set
                return clazz == item.getClazz();
            }
        };
    }

}
