package ch.jalu.testmodule.services;

import javax.inject.Inject;

/**
 * Simple service for sample injection setup.
 */
public class TwoService {

    @Inject
    private OneService oneService;

    TwoService() {
    }

    public String getName() {
        return oneService.getName();
    }
}
