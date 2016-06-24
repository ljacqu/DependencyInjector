package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample - class dependent on alpha service.
 */
public class GammaService implements Reloadable {

    private AlphaService alphaService;

    @Inject
    public GammaService(AlphaService alphaService) {
        this.alphaService = alphaService;
    }
}
