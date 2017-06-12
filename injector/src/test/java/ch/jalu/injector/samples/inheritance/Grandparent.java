package ch.jalu.injector.samples.inheritance;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;

import javax.inject.Inject;

/**
 * Grandparent.
 */
public class Grandparent {

    @Inject
    private AlphaService alphaService;

    @Inject
    private BetaManager betaManager;

    public AlphaService getAlphaService() {
        return alphaService;
    }

    public BetaManager getBetaManager() {
        return betaManager;
    }
}
