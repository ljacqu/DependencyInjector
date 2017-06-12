package ch.jalu.injector.samples.vehicles;

import ch.jalu.injector.samples.vehicles.services.IdentificationService;
import ch.jalu.injector.samples.vehicles.services.SailService;

import javax.inject.Inject;

/**
 * Ship.
 */
public class Ship implements VehicleWithHorn {

    @Inject
    private IdentificationService identificationService;
    @Inject
    private SailService sailService;

    @Override
    public void startJourney() {

    }

    @Override
    public String identify() {
        return identificationService.createId(this, "Ship McShipface");
    }

    @Override
    public void honk() {
        System.out.println("Awooooooooooga");
    }
}
