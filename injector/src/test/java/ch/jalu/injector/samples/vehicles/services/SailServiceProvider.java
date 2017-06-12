package ch.jalu.injector.samples.vehicles.services;

import ch.jalu.injector.samples.AlphaService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Instantiates sail service.
 */
public class SailServiceProvider implements Provider<SailService> {

    @Inject
    private AlphaService alphaService;

    @Override
    public SailService get() {
        return new SailService(alphaService, 3);
    }
}
