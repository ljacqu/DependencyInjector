package ch.jalu.injector.samples.animals;

import ch.jalu.injector.samples.animals.services.CroakService;
import ch.jalu.injector.samples.animals.services.NameService;

import javax.inject.Inject;

/**
 * Frog.
 */
public class Frog implements Animal {

    @Inject
    private NameService nameService;
    @Inject
    private CroakService croakService;

    @Override
    public String getName() {
        return nameService.constructName(this);
    }

    @Override
    public boolean canFly() {
        return false;
    }

    public String makeSound() {
        return croakService.makeSound();
    }
}
