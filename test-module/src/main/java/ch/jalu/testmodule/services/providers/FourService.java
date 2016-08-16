package ch.jalu.testmodule.services.providers;

import ch.jalu.testmodule.services.ThreeService;

/**
 * Simple service for sample injection setup.
 */
public class FourService {

    private final ThreeService threeService;

    public FourService(ThreeService threeService) {
        this.threeService = threeService;
    }

    public String getName() {
        return threeService.getName();
    }
}
