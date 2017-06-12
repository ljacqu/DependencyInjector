package ch.jalu.injector.extras.handlers;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.extras.samples.animals.Animal;
import ch.jalu.injector.extras.samples.animals.AnimalLister;
import ch.jalu.injector.extras.samples.animals.Bird;
import ch.jalu.injector.extras.samples.animals.Chicken;
import ch.jalu.injector.extras.samples.animals.Frog;
import ch.jalu.injector.extras.samples.animals.Lion;
import ch.jalu.injector.extras.samples.animals.Ostrich;
import ch.jalu.injector.extras.samples.animals.Reptile;
import ch.jalu.injector.extras.samples.animals.Snake;
import ch.jalu.injector.extras.samples.animals.Sparrow;
import ch.jalu.injector.extras.samples.animals.Turtle;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link AllTypesAnnotationHandler}.
 */
public class AllTypesAnnotationHandlerTest {

    private Injector injector;

    @Before
    public void initializeInjector() {
        String rootPackage = "ch.jalu.injector.extras.samples.animals";
        injector = new InjectorBuilder()
            .addHandlers(new AllTypesAnnotationHandler(rootPackage))
            .addDefaultHandlers(rootPackage)
            .create();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldInitializeCorrectly() throws Exception {
        // given / when
        AnimalLister animalLister = injector.getSingleton(AnimalLister.class);
        Set<Class<Animal>> animalTypes = animalLister.getAnimalTypes();

        // then
        assertThat(animalTypes, containsInAnyOrder(Bird.class, Chicken.class, Frog.class, Lion.class,
            Ostrich.class, Reptile.class, Snake.class, Sparrow.class, Turtle.class));
    }
}
