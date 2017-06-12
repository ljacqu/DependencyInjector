package ch.jalu.injector.samples.vehicles.services;

import ch.jalu.injector.samples.vehicles.Vehicle;

/**
 * Helps vehicles to identify themselves.
 */
public class IdentificationService {

    public String createId(Vehicle vehicle, String id) {
        return vehicle.getClass().getSimpleName() + "-" + id;
    }
}
