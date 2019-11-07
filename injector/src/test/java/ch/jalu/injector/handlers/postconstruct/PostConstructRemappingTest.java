package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.vehicles.Bicycle;
import ch.jalu.injector.samples.vehicles.Plane;
import ch.jalu.injector.samples.vehicles.VehiclesJourneyService;
import ch.jalu.injector.samples.vehicles.services.DriveService;
import ch.jalu.injector.samples.vehicles.services.SailService;
import ch.jalu.injector.samples.vehicles.services.SailServiceProvider;
import ch.jalu.injector.samples.vehicles.services.SteerServiceManager;
import javassist.util.proxy.Proxy;
import org.junit.jupiter.api.Test;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Tests that handler implementations can change the object that will be stored on post construct.
 */
public class PostConstructRemappingTest {

    private static final String PACKAGE = "ch.jalu.injector";

    @Test
    public void shouldInitializeProfiledClassesWithProxy() {
        // given
        InjectorImpl injector = (InjectorImpl) new InjectorBuilder()
            .addDefaultHandlers(PACKAGE)
            .create();
        ProfilePostConstructHandler profileHandler = new ProfilePostConstructHandler(injector);
        injector.getConfig().addHandlers(singletonList(profileHandler));
        injector.register(ProvidedClass.class, new ProvidedClass(""));
        injector.registerProvider(SailService.class, SailServiceProvider.class);

        // when (trigger initialization + invoke some methods)
        VehiclesJourneyService journeyService = injector.getSingleton(VehiclesJourneyService.class);
        journeyService.startJourney();
        SteerServiceManager steerManager = injector.getSingleton(SteerServiceManager.class);
        steerManager.brakeAll();
        injector.getSingleton(Plane.class).identify();

        // then
        assertThat(profileHandler.getInvocations(), contains("Car#startJourney", "DriveService#brake",
            "SteerServiceManager#brakeAll", "DriveService#brake", "Plane#identify"));
        assertThat(injector.getIfAvailable(Plane.class), instanceOf(Proxy.class));
        assertThat(injector.getIfAvailable(Bicycle.class), not(instanceOf(Proxy.class)));
        assertThat(injector.getIfAvailable(DriveService.class), instanceOf(Proxy.class));
    }
}
