package ch.jalu.injector.samples;

import lombok.Getter;

import javax.inject.Inject;

/**
 * Sample - class dependent on alpha service.
 */
@Getter
public class GammaService implements Reloadable {

    private AlphaService alphaService;

    @Inject
    public GammaService(AlphaService alphaService) {
        this.alphaService = alphaService;
    }
}
