package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.ChirpService;

import javax.inject.Inject;

/**
 *
 */
public class Chicken extends Bird {

    @Inject
    Chicken(ChirpService chirpService) {
        super(chirpService);
    }

    @Override
    public long weightInGramms() {
        return 2400;
    }

    @Override
    public String getName() {
        return "Chicken";
    }
}
