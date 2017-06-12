package ch.jalu.injector.samples.vehicles;

import java.util.UUID;

/**
 * Vehicles that don't have some sort of identification number / name.
 */
public abstract class UnidentifiableVehicle implements Vehicle {

    @Override
    public String identify() {
        return UUID.randomUUID().toString();
    }
}
