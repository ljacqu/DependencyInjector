package ch.jalu.injector.samples.vehicles.services;

import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;

public class DriveService implements SteeringService {
    @Override
    @ProfilePostConstructHandler.Profile
    public void brake() {

    }

    @Override
    public void steer() {

    }
}
