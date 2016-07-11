package ch.jalu.testmodule.services;

import javax.inject.Inject;

/**
 * Simple service for sample injection setup.
 */
public class ThreeService {

    private final String name;

    @Inject
    ThreeService(TwoService twoService) {
        this.name = twoService.getName();
    }

    public String getName() {
        return name;
    }

}
