package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.annotations.AllInstances;
import ch.jalu.injector.extras.annotations.AllTypes;
import ch.jalu.injector.extras.samples.animals.services.SoundService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Entry point.
 */
public class AnimalHandler {

    public final List<Animal> animals;
    public final Set<Class<SoundService>> soundServiceTypes;

    @Inject
    protected AnimalHandler(@AllInstances List<Animal> animals,
                            @AllTypes(SoundService.class) Set<Class<SoundService>> soundServiceTypes) {
        this.animals = Collections.unmodifiableList(animals);
        this.soundServiceTypes = Collections.unmodifiableSet(soundServiceTypes);
    }

    public List<String> gatherAllSounds() {
        List<String> sounds = new ArrayList<>(animals.size());
        for (Animal animal : animals) {
            sounds.add(animal.makeSound());
        }
        return sounds;
    }

    public List<String> getAllNames() {
        List<String> names = new ArrayList<>(animals.size());
        for (Animal animal : animals) {
            names.add(animal.getName());
        }
        return names;
    }
}
