package ch.jalu.testmodule.services.providers;

import ch.jalu.testmodule.services.TwoService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider for {@link FiveServiceProvider}.
 */
public class FiveServiceProviderProvider implements Provider<FiveServiceProvider> {

    @Inject
    private TwoService twoService;

    @Inject
    private FourService fourService;

    @Override
    public FiveServiceProvider get() {
        return FiveServiceProvider.create(fourService, twoService);
    }
}
