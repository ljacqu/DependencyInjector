package ch.jalu.injector;

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
import ch.jalu.injector.samples.Size;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link InjectorImpl}.
 */
public class InjectorImplTest {

    private final String ALLOWED_PACKAGE = getClass().getPackage().getName() + ".samples";

    private InjectorImpl injector;

    // As we test many cases that throw exceptions, we use JUnit's ExpectedException Rule
    // to make sure that we receive the exception we expect
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setInitializer() {
        injector = new InjectorImpl(ALLOWED_PACKAGE);
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldInitializeElements() {
        // given / when
        BetaManager betaManager = injector.get(BetaManager.class);

        // then
        assertThat(betaManager, not(nullValue()));
        for (Object o : betaManager.getDependencies()) {
            assertThat(o, not(nullValue()));
        }
    }

    @Test
    public void shouldThrowForInvalidPackage() {
        // given / when / then
        expectInjectorException("outside of the allowed packages");
        injector.get(InvalidClass.class);
    }

    @Test
    public void shouldThrowForUnregisteredPrimitiveType() {
        // given / when / then
        expectInjectorException("Primitive types must be provided");
        injector.get(int.class);
    }

    @Test
    public void shouldPassValueByAnnotation() {
        // given
        int size = 12;
        long duration = -15482L;
        injector.provide(Size.class, size);
        injector.provide(Duration.class, duration);

        // when
        ClassWithAnnotations object = injector.get(ClassWithAnnotations.class);

        // then
        assertThat(object, not(nullValue()));
        assertThat(object.getSize(), equalTo(size));
        assertThat(object.getDuration(), equalTo(duration));
        // some sample check to make sure we only have one instance of GammaService
        assertThat(object.getGammaService(), equalTo(injector.get(BetaManager.class).getDependencies()[1]));
    }

    @Test
    public void shouldRecognizeCircularReferences() {
        // given / when / then
        expectInjectorException("Found cyclic dependency");
        injector.get(CircularClasses.Circular3.class);
    }

    @Test
    public void shouldThrowForUnregisteredAnnotation() {
        // given
        injector.provide(Size.class, 4523);

        // when / then
        expectInjectorException("must be registered beforehand");
        injector.get(ClassWithAnnotations.class);
    }

    @Test
    public void shouldThrowForFieldInjectionWithoutNoArgsConstructor() {
        // given / when / then
        expectInjectorException("Did not find injection method");
        injector.get(BadFieldInjection.class);
    }

    @Test
    public void shouldInjectFieldsWithAnnotationsProperly() {
        // given
        injector.provide(Size.class, 2809375);
        injector.provide(Duration.class, 13095L);

        // when
        FieldInjectionWithAnnotations result = injector.get(FieldInjectionWithAnnotations.class);

        // then
        assertThat(result.getSize(), equalTo(2809375));
        assertThat(result.getDuration(), equalTo(13095L));
        assertThat(result.getBetaManager(), not(nullValue()));
        assertThat(result.getClassWithAnnotations(), not(nullValue()));
        assertThat(result.getClassWithAnnotations().getGammaService(),
            equalTo(result.getBetaManager().getDependencies()[1]));
    }

    @Test
    public void shouldThrowForAnnotationAsKey() {
        // given / when / then
        expectInjectorException("Cannot retrieve annotated elements in this way");
        injector.get(Size.class);
    }

    @Test
    public void shouldThrowForSecondRegistration() {
        // given / when / then
        expectInjectorException("There is already an object present");
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldThrowForSecondAnnotationRegistration() {
        // given
        injector.provide(Size.class, 12);

        // when / then
        expectInjectorException("already registered");
        injector.provide(Size.class, -8);
    }

    @Test
    public void shouldThrowForNullValueAssociatedToAnnotation() {
        // given / when / then
        expectInjectorException("may not be null");
        injector.provide(Duration.class, null);
    }

    @Test
    public void shouldThrowForRegisterWithNull() {
        // given / when / then
        expectInjectorException("may not be null");
        injector.register(String.class, null);
    }

    @Test
    public void shouldExecutePostConstructMethod() {
        // given
        injector.provide(Size.class, 15123);

        // when
        PostConstructTestClass testClass = injector.get(PostConstructTestClass.class);

        // then
        assertThat(testClass.wasPostConstructCalled(), equalTo(true));
        assertThat(testClass.getBetaManager(), not(nullValue()));
    }

    @Test
    public void shouldThrowForInvalidPostConstructMethod() {
        // given / when / then
        expectInjectorException("@PostConstruct method may not be static or have any parameters");
        injector.get(InvalidPostConstruct.WithParams.class);
    }

    @Test
    public void shouldThrowForStaticPostConstructMethod() {
        // given / when / then
        expectInjectorException("@PostConstruct method may not be static or have any parameters");
        injector.get(InvalidPostConstruct.Static.class);
    }

    @Test
    public void shouldForwardExceptionFromPostConstruct() {
        // given / when / then
        expectInjectorException("Error executing @PostConstruct method");
        injector.get(InvalidPostConstruct.ThrowsException.class);
    }

    @Test
    public void shouldThrowForMultiplePostConstructMethods() {
        // given / when / then
        expectInjectorException("Multiple methods with @PostConstruct");
        injector.get(InvalidPostConstruct.MultiplePostConstructs.class);
    }

    @Test
    public void shouldThrowForPostConstructNotReturningVoid() {
        // given / when / then
        expectInjectorException("@PostConstruct method must have return type void");
        injector.get(InvalidPostConstruct.NotVoidReturnType.class);
    }

    @Test
    public void shouldThrowForAbstractNonRegisteredDependency() {
        // given / when / then
        expectInjectorException("cannot be instantiated");
        injector.get(ClassWithAbstractDependency.class);
    }

    @Test
    public void shouldInstantiateWithImplementationOfAbstractDependency() {
        // given
        ClassWithAbstractDependency.ConcreteDependency concrete = new ClassWithAbstractDependency.ConcreteDependency();
        injector.register(ClassWithAbstractDependency.AbstractDependency.class, concrete);

        // when
        ClassWithAbstractDependency cwad = injector.get(ClassWithAbstractDependency.class);

        // then
        assertThat(cwad.getAbstractDependency() == concrete, equalTo(true));
        assertThat(cwad.getAlphaService(), not(nullValue()));
    }

    @Test
    public void shouldThrowForAlreadyRegisteredClass() {
        // given
        injector.register(BetaManager.class, new BetaManager());

        // when / then
        expectInjectorException("There is already an object present");
        injector.register(BetaManager.class, new BetaManager());
    }

    @Test
    public void shouldCreateNewUntrackedInstance() {
        // given / when
        AlphaService singletonScoped = injector.get(AlphaService.class);
        AlphaService requestScoped = injector.newInstance(AlphaService.class);

        // then
        assertThat(singletonScoped.getProvidedClass(), not(nullValue()));
        assertThat(singletonScoped.getProvidedClass(), equalTo(requestScoped.getProvidedClass()));
        assertThat(singletonScoped, not(sameInstance(requestScoped)));
    }

    @Test
    public void shouldThrowForStaticFieldInjection() {
        // given / when / then
        expectInjectorException("is static but annotated with @Inject");
        injector.newInstance(InvalidStaticFieldInjection.class);
    }

    @Test
    public void shouldFallbackToSimpleInstantiationForPlainClass() {
        // given / when
        InstantiationFallbackClasses.HasFallbackDependency result =
            injector.get(InstantiationFallbackClasses.HasFallbackDependency.class);

        // then
        assertThat(result, not(nullValue()));
        assertThat(result.getGammaService(), not(nullValue()));
        assertThat(result.getFallbackDependency(), not(nullValue()));
    }

    @Test
    public void shouldRetrieveExistingInstancesOnly() {
        // given
        injector.get(GammaService.class);

        // when
        AlphaService alphaService = injector.getIfAvailable(AlphaService.class);
        BetaManager betaManager = injector.getIfAvailable(BetaManager.class);

        // then
        // was initialized because is dependency of GammaService
        assertThat(alphaService, not(nullValue()));
        // nothing caused this to be initialized
        assertThat(betaManager, nullValue());
    }

    private void expectInjectorException(String message) {
        expectedException.expect(InjectorException.class);
        expectedException.expectMessage(containsString(message));
    }

}
