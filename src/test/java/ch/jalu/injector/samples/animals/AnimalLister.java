package ch.jalu.injector.samples.animals;

import ch.jalu.injector.annotations.AllTypes;

import javax.inject.Inject;
import java.util.Collections;
import java.util.Set;

/**
 * Knows of all animal subtypes by injection.
 */
public class AnimalLister {

    @Inject
    @AllTypes(Animal.class)
    private Set<Class<Animal>> animals;

    public AnimalLister() { }

    public Set<Class<Animal>> getAnimalTypes() {
        return Collections.unmodifiableSet(animals);
    }
}
