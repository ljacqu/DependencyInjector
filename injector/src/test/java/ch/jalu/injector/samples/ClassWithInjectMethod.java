package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class with an {@code @Inject} method.
 */
public class ClassWithInjectMethod {

    @Inject
    private AlphaService alphaService;
    @Inject
    private BetaManager betaManager;

    private GammaService gammaService;

    @Inject
    public void setGammaService(GammaService gammaService) {
        this.gammaService = gammaService;
    }
}
