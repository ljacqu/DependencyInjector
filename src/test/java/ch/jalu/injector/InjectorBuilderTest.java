package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.preconstruct.PreConstructPackageValidator;
import ch.jalu.injector.handlers.testimplementations.ImplementationClassHandler;
import ch.jalu.injector.handlers.testimplementations.ListeningDependencyHandler;
import ch.jalu.injector.handlers.testimplementations.ThrowingPostConstructHandler;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.animals.Animal;
import ch.jalu.injector.samples.animals.Bird;
import ch.jalu.injector.samples.animals.Chicken;
import ch.jalu.injector.samples.animals.Reptile;
import ch.jalu.injector.samples.animals.Snake;
import ch.jalu.injector.samples.animals.Sparrow;
import ch.jalu.injector.samples.animals.services.HissService;
import ch.jalu.injector.samples.animals.services.HissServiceProvider;
import ch.jalu.injector.samples.subpackage.SubpackageClass;
import org.junit.Test;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link InjectorBuilder}.
 */
public class InjectorBuilderTest {

    /**
     * Tests that the allowed packages are set from the builder ot the injector,
     * and tests that the feature works as expected in {@link InjectorImpl}.
     */
    @Test
    public void shouldSupplyInjectorWithPackageSetting() {
        // create injector via builder
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers(getClass().getPackage().getName() + ".samples.subpackage")
            .create();

        // register AlphaService and GammaService
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        GammaService gammaService = new GammaService(alphaService);
        injector.register(AlphaService.class, alphaService);
        injector.register(GammaService.class, gammaService);

        // make sure we can instantiate SubpackageClass
        SubpackageClass subpackageClass = injector.getSingleton(SubpackageClass.class);
        assertThat(subpackageClass, not(nullValue()));

        // expect exception if we try to instantiate something outside of subpackage
        try {
            injector.getSingleton(BetaManager.class);
            fail("Expected exception");
        } catch (InjectorException ex) {
            assertThat(ex.getMessage(), containsString("outside of the allowed packages"));
        }
    }

    /**
     * Tests the order of handlers (important!) and that custom handlers are
     */
    @Test
    public void shouldAllowCustomHandlers() {
        // Instantiate handlers
        ImplementationClassHandler implementationClassHandler = new ImplementationClassHandler();
        implementationClassHandler.register(Animal.class, Reptile.class);
        implementationClassHandler.register(Reptile.class, Snake.class);
        implementationClassHandler.register(Bird.class, Sparrow.class);
        PreConstructPackageValidator packageValidator = new PreConstructPackageValidator("ch.jalu");

        SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();
        ListeningDependencyHandler listeningDependencyHandler = new ListeningDependencyHandler();

        PostConstructHandler postConstructHandler = new PostConstructMethodInvoker();
        // throw when Chicken gets instantiated
        ThrowingPostConstructHandler throwingPostConstructHandler = new ThrowingPostConstructHandler(Chicken.class);

        // Create Injector with all handlers
        InjectorBuilder builder = new InjectorBuilder();
        Injector injector = builder
            .addHandlers(implementationClassHandler, packageValidator, savedAnnotationsHandler,
                         listeningDependencyHandler, postConstructHandler, throwingPostConstructHandler)
            .addHandlers(InjectorBuilder.createInstantiationProviders())
            .create();

        // Check presence of handlers and their order
        InjectorConfig config = ((InjectorImpl) injector).getConfig();
        assertThat(config.getPreConstructHandlers(), contains(implementationClassHandler, packageValidator));
        assertThat(config.getDependencyHandlers(), contains(savedAnnotationsHandler, listeningDependencyHandler));
        assertThat(config.getPostConstructHandlers(), contains(postConstructHandler, throwingPostConstructHandler));

        // Set Provider for HissService
        injector.registerProvider(HissService.class, HissServiceProvider.class);

        // Request Animal singleton -> mapped to Reptile -> Snake
        Animal animal = injector.getSingleton(Animal.class);
        assertThat(animal, instanceOf(Snake.class));
        Snake snake = injector.getSingleton(Snake.class);
        assertThat(snake, sameInstance(animal));

        // Check counts
        assertThat(implementationClassHandler.getCounter(), equalTo(4)); // Snake, Configuration, HissServiceProvider, HissService
        // Snake depends on Configuration and HissService (2)
        // HissService comes from HissServiceProvider, which needs to be instantiated (1)
        // and it depends on Configuration (1) = 4.
        assertThat(listeningDependencyHandler.getCounter(), equalTo(4));
        assertThat(throwingPostConstructHandler.getCounter(), equalTo(4)); // Snake, Configuration, HissService, HissServiceProvider

        // Check correct behavior of ThrowingPostHandler
        try {
            injector.getSingleton(Chicken.class);
            fail("Expected exception to occur");
        } catch (InjectorException e) {
            // noop
        }
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowExceptionForUnknownHandlerType() {
        // given
        Handler handler = new UnknownHandler();

        // when
        new InjectorBuilder().addHandlers(handler);

        // then - expect exception
    }

    private static final class UnknownHandler implements Handler {
    }
}
