package ch.jalu.testmodule.services.providers;

import ch.jalu.injector.Injector;
import ch.jalu.testmodule.services.ThreeService;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider for {@link FourService}.
 */
public class FourServiceProvider implements Provider<FourService> {

    @Inject
    private Injector injector;

    /**
     * Constructor for injection.
     */
    private FourServiceProvider() {
    }

    /**
     * Manual constructor.
     *
     * @param injector the injector to retrieve ThreeService with
     */
    public FourServiceProvider(Injector injector) {
        this.injector = injector;
    }

    @Override
    public FourService get() {
        return new FourService(injector.getSingleton(ThreeService.class));
    }
}
