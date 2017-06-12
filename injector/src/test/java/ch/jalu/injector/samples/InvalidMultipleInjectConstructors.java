package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample: invalid class with two @Inject constructors.
 */
public class InvalidMultipleInjectConstructors {

    private final AlphaService alphaService;
    private final GammaService gammaService;

    @Inject
    InvalidMultipleInjectConstructors(AlphaService alphaService, GammaService gammaService) {
        this.alphaService = alphaService;
        this.gammaService = gammaService;
    }

    @Inject
    InvalidMultipleInjectConstructors(AlphaService alphaService, BetaManager betaManager, GammaService gammaService) {
        this.alphaService = alphaService;
        this.gammaService = gammaService;
    }
}
