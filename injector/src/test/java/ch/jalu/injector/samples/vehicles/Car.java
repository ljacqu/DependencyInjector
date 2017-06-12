package ch.jalu.injector.samples.vehicles;

import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;
import ch.jalu.injector.samples.vehicles.services.DriveService;
import ch.jalu.injector.samples.vehicles.services.IdentificationService;

import javax.inject.Inject;

/**
 * Car.
 */
public class Car implements VehicleWithHorn {

    @Inject
    private DriveService driveService;
    @Inject
    private IdentificationService identificationService;

    @Override
    @ProfilePostConstructHandler.Profile
    public void startJourney() {
        driveService.steer();
        driveService.brake();
    }

    @Override
    public String identify() {
        return identificationService.createId(this, "A93ZYX");
    }

    @Override
    public void honk() {
        System.out.println("Beep beep!");
    }
}
