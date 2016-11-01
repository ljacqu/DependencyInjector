package ch.jalu.injector.extras.samples.animals;

/**
 * Reptile superclass.
 */
public abstract class Reptile implements Animal {

    @Override
    public boolean canFly() {
        return false;
    }

}
