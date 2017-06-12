package ch.jalu.injector.extras.samples.animals;


import ch.jalu.injector.extras.samples.animals.services.NameService;
import ch.jalu.injector.extras.samples.animals.services.SqueakService;

import javax.inject.Inject;

public class Turtle extends Reptile {

    @Inject
    private SqueakService squeakService;

    @Inject
    private NameService nameService;

    @Override
    public String makeSound() {
        return squeakService.makeSound();
    }

    @Override
    public String getName() {
        return nameService.constructName(this);
    }
}
