package ch.jalu.injector.samples.animals.services;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider for {@link HissService}.
 */
public class HissServiceProvider implements Provider<HissService> {

    private Configuration configuration;

    @Inject
    public HissServiceProvider(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public HissService get() {
        return new HissService(configuration);
    }

}
