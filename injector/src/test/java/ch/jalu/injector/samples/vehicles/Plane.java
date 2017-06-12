package ch.jalu.injector.samples.vehicles;

import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;
import ch.jalu.injector.samples.vehicles.services.PilotService;

import javax.inject.Inject;

/**
 * Plane.
 */
public class Plane implements Vehicle {

    private final PilotService pilotService;

    @Inject
    Plane(PilotService pilotService) {
        this.pilotService = pilotService;
    }

    @Override
    public void startJourney() {
        pilotService.steer();
    }

    @Override
    @ProfilePostConstructHandler.Profile
    public String identify() {
        return "YY 7239";
    }
}
