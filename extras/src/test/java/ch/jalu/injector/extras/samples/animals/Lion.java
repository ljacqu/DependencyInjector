package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.NameService;
import ch.jalu.injector.extras.samples.animals.services.RoarService;

import javax.inject.Inject;

/**
 * Lion class.
 */
public class Lion extends Mammal {

    @Inject
    private RoarService roarService;
    @Inject
    private NameService nameService;

    @Override
    public boolean canFly() {
        return false;
    }

    @Override
    public String makeSound() {
        return roarService.makeSound();
    }

    @Override
    public String getName() {
        return nameService.constructName(this);
    }

}
