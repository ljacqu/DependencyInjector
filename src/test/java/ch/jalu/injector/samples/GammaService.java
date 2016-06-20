package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample - class dependent on alpha service.
 */
public class GammaService {

    private AlphaService alphaService;
    private boolean wasReloaded;

    @Inject
    public GammaService(AlphaService alphaService) {
        this.alphaService = alphaService;
    }

    public AlphaService getAlphaService() {
        return alphaService;
    }

    public boolean getWasReloaded() {
        return wasReloaded;
    }
}
