package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.ChirpService;
import ch.jalu.injector.extras.samples.animals.services.NameService;

import javax.inject.Inject;

/**
 *
 */
public class Ostrich extends Bird {

    private NameService nameService;

    @Inject
    Ostrich(ChirpService chirpService, NameService nameService) {
        super(chirpService);
        this.nameService = nameService;
    }

    @Override
    public long weightInGramms() {
        return 90 * 1000;
    }

    @Override
    public String getName() {
        return nameService.constructName(this);
    }

    @Override
    public boolean canFly() {
        return false;
    }
}
