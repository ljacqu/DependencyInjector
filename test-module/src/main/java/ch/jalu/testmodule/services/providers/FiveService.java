package ch.jalu.testmodule.services.providers;

/**
 * Sample service.
 */
public class FiveService {

    private final FourService fourService;

    private FiveService(FourService fourService) {
        this.fourService = fourService;
    }

    public static FiveService create(FourService fourService) {
        return new FiveService(fourService);
    }

    public String getName() {
        return fourService.getName();
    }
}
