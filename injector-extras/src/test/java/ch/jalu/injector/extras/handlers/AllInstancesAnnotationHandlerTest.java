package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.extras.AllInstances;
import ch.jalu.injector.extras.AllTypes;
import ch.jalu.injector.extras.samples.CinnamonService;
import ch.jalu.injector.extras.samples.LilacService;
import ch.jalu.injector.extras.samples.PaprikaService;
import ch.jalu.injector.extras.samples.SpiceService;
import ch.jalu.injector.extras.samples.animals.Animal;
import ch.jalu.injector.extras.samples.animals.AnimalHandler;
import ch.jalu.injector.extras.samples.animals.services.ChirpService;
import ch.jalu.injector.extras.samples.animals.services.Configuration;
import ch.jalu.injector.extras.samples.animals.services.CroakService;
import ch.jalu.injector.extras.samples.animals.services.HissService;
import ch.jalu.injector.extras.samples.animals.services.HissServiceProvider;
import ch.jalu.injector.extras.samples.animals.services.RoarService;
import ch.jalu.injector.extras.samples.animals.services.SoundServiceSupervisor;
import ch.jalu.injector.extras.samples.animals.services.SqueakService;
import org.junit.Before;
import org.junit.Test;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AllInstancesAnnotationHandler}.
 */
public class AllInstancesAnnotationHandlerTest {

    private static final String ROOT_PACKAGE = "ch.jalu.injector";

    private Injector injector;

    @Before
    public void initializeInjector() {
        AllInstancesAnnotationHandler allInstancesHandler = new AllInstancesAnnotationHandler(ROOT_PACKAGE);
        AllTypesAnnotationHandler allTypesHandler = new AllTypesAnnotationHandler(ROOT_PACKAGE);
        injector = new InjectorBuilder()
                .addHandlers(allInstancesHandler, allTypesHandler)
                .addDefaultHandlers(ROOT_PACKAGE)
                .create();
        injector.registerProvider(HissService.class, HissServiceProvider.class);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInstantiateAllSubTypes() {
        // given / when
        SoundServiceSupervisor supervisor = injector.getSingleton(SoundServiceSupervisor.class);

        // then
        assertThat(supervisor.getSoundServices(), arrayContainingInAnyOrder(
            instanceOf(ChirpService.class), instanceOf(CroakService.class), instanceOf(HissService.class),
            instanceOf(RoarService.class), instanceOf(SqueakService.class)));
    }

    @Test
    public void shouldInstantiateSubTypesWithDeclaredType() {
        // given / when
        CorrectFields correctFields = injector.getSingleton(CorrectFields.class);

        // then
        assertThat(correctFields.spiceServices, containsInAnyOrder(instanceOf(CinnamonService.class), instanceOf(PaprikaService.class)));
        assertThat(correctFields.animals, hasSize(7));
        assertThat(correctFields.animals, not(hasItem(nullValue(Animal.class))));
        assertThat(correctFields.lilacServices, empty());
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForInvalidFieldType() {
        // given / when / then
        injector.getSingleton(InvalidFields.class);
    }

    @Test(expected = InjectorException.class)
    public void shouldThrowForMissingGenericType() {
        // given / when / then
        injector.getSingleton(MissingGenericType.class);
    }

    /**
     * Full integration test with focus on {@link AllInstances} and {@link AllTypes}.
     *
     * @see AnimalHandler
     */
    @Test
    @SuppressWarnings("unchecked")
    public void shouldPerformFullIntegrationTest() {
        // Configuration setup
        Configuration configuration = injector.getSingleton(Configuration.class);
        configuration.setLang("de");

        // Get AnimalHandler: it has @AllInstances for Animal
        AnimalHandler handler = injector.getSingleton(AnimalHandler.class);
        assertThat(handler.animals, hasSize(7));
        assertThat(handler.soundServiceTypes, hasSize(5));

        // Get names of all animals and expect German names
        assertThat(handler.getAllNames(), hasItems("Löwe", "Spatz", "Strauss", "Frosch", "Schlange"));

        // Get everyone's sounds (Turtle sound is translatable, so check that)
        List<String> sounds1 = handler.gatherAllSounds();
        assertThat(sounds1, hasSize(7));
        // no entry null
        assertThat(sounds1, not(hasItem(nullValue(String.class))));
        assertThat(sounds1, hasItem("krächz"));

        // Change config to English
        configuration.setLang("en");

        // Get SoundService supervisor to mute all sound services
        SoundServiceSupervisor soundSupervisor = injector.getSingleton(SoundServiceSupervisor.class);
        soundSupervisor.muteAll();

        // Get all sounds again -> expect only the Lion to have sound
        List<String> sounds2 = handler.gatherAllSounds();
        assertThat(sounds2, containsInAnyOrder(equalTo("Roar"),
            nullValue(), nullValue(), nullValue(), nullValue(), nullValue(), nullValue()));

        // Get all names, this time in English
        assertThat(handler.getAllNames(), hasItems("Chicken", "Sparrow", "Turtle", "snake", "Ostrich"));
    }

    private static final class CorrectFields {
        @Inject
        @AllInstances
        private Collection<SpiceService> spiceServices;

        @Inject
        @AllInstances
        private Set<Animal> animals;

        @Inject
        @AllInstances
        private List<LilacService> lilacServices;
    }

    private static final class InvalidFields {
        @Inject
        @AllInstances
        private Provider<Animal> objects;
    }

    private static final class MissingGenericType {
        @Inject
        @AllInstances
        private Collection animals;
    }

}