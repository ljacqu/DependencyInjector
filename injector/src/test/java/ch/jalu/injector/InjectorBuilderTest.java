package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructMethodInvoker;
import ch.jalu.injector.handlers.testimplementations.ImplementationClassHandler;
import ch.jalu.injector.handlers.testimplementations.ListeningDependencyHandler;
import ch.jalu.injector.handlers.testimplementations.ThrowingPostConstructHandler;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.subpackage.SubpackageClass;
import ch.jalu.injector.samples.vehicles.Car;
import ch.jalu.injector.samples.vehicles.Ship;
import ch.jalu.injector.samples.vehicles.ShoppingCart;
import ch.jalu.injector.samples.vehicles.UnidentifiableVehicle;
import ch.jalu.injector.samples.vehicles.Vehicle;
import ch.jalu.injector.samples.vehicles.VehicleWithHorn;
import ch.jalu.injector.samples.vehicles.services.SailService;
import ch.jalu.injector.samples.vehicles.services.SailServiceProvider;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Tests the order of handlers (important!) and that custom handlers are included.
     */
    @Test
    public void shouldAllowCustomHandlers() {
        // Instantiate handlers
        ImplementationClassHandler implementationClassHandler = new ImplementationClassHandler();
        implementationClassHandler.register(Vehicle.class, UnidentifiableVehicle.class);
        implementationClassHandler.register(UnidentifiableVehicle.class, ShoppingCart.class);
        implementationClassHandler.register(VehicleWithHorn.class, Ship.class);

        SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();
        ListeningDependencyHandler listeningDependencyHandler = new ListeningDependencyHandler();

        Handler postConstructHandler = new PostConstructMethodInvoker();
        // throw when Car gets instantiated
        ThrowingPostConstructHandler throwingPostConstructHandler = new ThrowingPostConstructHandler(Car.class);

        // Create Injector with all handlers
        List<Handler> instantiationProviders = InjectorBuilder.createInstantiationProviders("ch.jalu");
        Injector injector = new InjectorBuilder()
            .addHandlers(implementationClassHandler, savedAnnotationsHandler,
                         listeningDependencyHandler, postConstructHandler, throwingPostConstructHandler)
            .addHandlers(instantiationProviders)
            .create();
        injector.register(ProvidedClass.class, new ProvidedClass(""));

        // Check presence of handlers and their order
        InjectorConfig config = ((InjectorImpl) injector).getConfig();
        List<Handler> handlers = new ArrayList<>();
        handlers.addAll(Arrays.asList(implementationClassHandler, savedAnnotationsHandler,
            listeningDependencyHandler, postConstructHandler, throwingPostConstructHandler));
        handlers.addAll(instantiationProviders);
        assertThat(config.getHandlers(), contains(handlers.toArray()));

        // Set Provider for SailService
        injector.registerProvider(SailService.class, SailServiceProvider.class);

        // Request Vehicle singleton -> mapped to UnidentifiableVehicle -> ShoppingCart
        Vehicle vehicle = injector.getSingleton(Vehicle.class);
        assertThat(vehicle, instanceOf(ShoppingCart.class));
        ShoppingCart cart = injector.getSingleton(ShoppingCart.class);
        assertThat(cart, sameInstance(vehicle));

        // Check counts
        assertThat(implementationClassHandler.getCounter(), equalTo(4)); // ShoppingCart, Alpha/Beta/Gamma
        // ShoppingCart depends on BetaManager (2)
        // BetaManager -> ProvidedClass, AlphaService, GammaService (3)
        // GammaService -> AlphaService (1)
        assertThat(listeningDependencyHandler.getCounter(), equalTo(6));
        assertThat(throwingPostConstructHandler.getCounter(), equalTo(4)); // ShoppingCart, Alpha/Beta/Gamma

        // Check correct behavior of ThrowingPostHandler
        try {
            injector.getSingleton(Car.class);
            fail("Expected exception to occur");
        } catch (InjectorException e) {
            // noop
        }
    }
}
