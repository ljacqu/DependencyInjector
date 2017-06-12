package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class - attempted field injection on a static member.
 */
public class StaticFieldInjection {

    @Inject
    private ProvidedClass providedClass;
    @Inject
    protected static AlphaService alphaService;

    StaticFieldInjection() { }

    public static AlphaService getAlphaService() {
        return alphaService;
    }

    public ProvidedClass getProvidedClass() {
        return providedClass;
    }
}
