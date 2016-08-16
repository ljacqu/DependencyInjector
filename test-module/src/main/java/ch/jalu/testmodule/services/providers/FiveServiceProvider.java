package ch.jalu.testmodule.services.providers;

import ch.jalu.testmodule.services.TwoService;

import javax.inject.Provider;
import java.util.Objects;

/**
 * Provider for {@link FiveService}.
 */
public class FiveServiceProvider implements Provider<FiveService> {

    private final FourService fourService;

    private FiveServiceProvider(FourService fourService, TwoService twoService) {
        Objects.requireNonNull(fourService);
        Objects.requireNonNull(twoService);
        this.fourService = fourService;
    }

    public static FiveServiceProvider create(FourService fourService, TwoService twoService) {
        return new FiveServiceProvider(fourService, twoService);
    }

    @Override
    public FiveService get() {
        return FiveService.create(fourService);
    }
}
