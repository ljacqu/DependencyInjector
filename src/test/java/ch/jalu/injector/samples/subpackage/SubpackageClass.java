package ch.jalu.injector.samples.subpackage;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.GammaService;

import javax.inject.Inject;

/**
 * Sample class in a subpackage.
 */
public class SubpackageClass {

    @Inject
    private AlphaService alphaService;

    @Inject
    private GammaService gammaService;

    SubpackageClass() { }

}
