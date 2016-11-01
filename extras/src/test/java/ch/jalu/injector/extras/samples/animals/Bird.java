package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.ChirpService;

/**
 * Bird.
 */
public abstract class Bird implements Animal {

    private ChirpService chirpService;

    public Bird(ChirpService chirpService) {
        this.chirpService = chirpService;
    }

    @Override
    public boolean canFly() {
        return true;
    }

    @Override
    public final String makeSound() {
        return chirpService.makeSound();
    }

    public abstract long weightInGramms();

}
