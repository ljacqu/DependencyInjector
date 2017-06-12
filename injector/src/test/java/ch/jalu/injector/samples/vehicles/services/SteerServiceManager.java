package ch.jalu.injector.samples.vehicles.services;

import ch.jalu.injector.factory.SingletonStore;
import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;

import javax.inject.Inject;

/**
 * Manages the {@link SteeringService} instances.
 */
public class SteerServiceManager {

    @Inject
    private SingletonStore<SteeringService> steeringServicesStore;

    @ProfilePostConstructHandler.Profile
    public void brakeAll() {
        steeringServicesStore.retrieveAllOfType()
            .forEach(SteeringService::brake);
    }
}
