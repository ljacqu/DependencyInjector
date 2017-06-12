package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample - depends on Provided, alpha and gamma.
 */
public class BetaManager {

    @Inject
    private ProvidedClass providedClass;
    @Inject
    private GammaService gammaService;
    @Inject
    private AlphaService alphaService;

    public Object[] getDependencies() {
        return new Object[]{providedClass, gammaService, alphaService};
    }
}
