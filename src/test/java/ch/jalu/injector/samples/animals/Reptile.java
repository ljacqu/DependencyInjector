package ch.jalu.injector.samples.animals;

/**
 * Reptile superclass.
 */
public abstract class Reptile implements Animal {

    @Override
    public boolean canFly() {
        return false;
    }

}
