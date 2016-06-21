package ch.jalu.injector.samples;

import lombok.Getter;

import javax.inject.Inject;

/**
 * Sample - field injection, including custom annotations.
 */
@Getter
public class FieldInjectionWithAnnotations {

    @Inject
    private BetaManager betaManager;
    @Inject
    @Size
    private int size;
    @Duration
    @Inject
    private long duration;
    @Inject
    protected ClassWithAnnotations classWithAnnotations;

    FieldInjectionWithAnnotations() {
    }
}
