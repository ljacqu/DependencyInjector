package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class with invalid final field.
 */
public class InvalidFinalInjectField {

    @Inject
    private AlphaService alphaService;

    @Inject
    private final GammaService gammaService = null;

}
