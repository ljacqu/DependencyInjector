package ch.jalu.injector.samples.vehicles;

import ch.jalu.injector.samples.BetaManager;

import javax.inject.Inject;

/**
 * Shopping cart.
 */
public class ShoppingCart extends UnidentifiableVehicle {

    @Inject
    private BetaManager betaManager;

    @Override
    public void startJourney() {
        throw new UnsupportedOperationException("ouch!");
    }
}
