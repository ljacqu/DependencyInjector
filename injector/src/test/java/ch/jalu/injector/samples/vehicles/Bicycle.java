package ch.jalu.injector.samples.vehicles;

import ch.jalu.injector.samples.vehicles.services.DriveService;

import javax.inject.Inject;

/**
 * Bicycle.
 */
public class Bicycle extends UnidentifiableVehicle {

    private final DriveService driveService;

    @Inject
    Bicycle(DriveService driveService) {
        this.driveService = driveService;
    }

    @Override
    public void startJourney() {
        driveService.steer();
    }
}
