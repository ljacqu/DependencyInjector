package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.ChirpService;
import ch.jalu.injector.extras.samples.animals.services.NameService;

import javax.inject.Inject;

public class Sparrow extends Bird {

    private NameService nameService;

    @Inject
    Sparrow(ChirpService chirpService, NameService nameService) {
        super(chirpService);
        this.nameService = nameService;
    }

    @Override
    public String getName() {
        return nameService.constructName(this);
    }

    @Override
    public long weightInGramms() {
        return 30;
    }
}
