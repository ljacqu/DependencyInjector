package ch.jalu.injector.samples.configurations;

import ch.jalu.injector.annotations.Provides;
import ch.jalu.injector.config.InjectorConfiguration;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;

/**
 * Sample configuration class.
 */
public class SampleConfiguration implements InjectorConfiguration {

    private BetaManager betaManager = new BetaManager();
    private ProvidedClass providedClass = new ProvidedClass("");

    @Provides
    public BetaManager initBetaManager() {
        return betaManager;
    }

    @Provides
    private ProvidedClass initProvidedClass() {
        return providedClass;
    }

    public ProvidedClass getProvidedClass() {
        return providedClass;
    }
}
