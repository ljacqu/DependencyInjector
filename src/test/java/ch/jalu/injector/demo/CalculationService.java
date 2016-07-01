package ch.jalu.injector.demo;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.math.BigDecimal;

/**
 * Calculation service.
 * <p>
 * Essentially the only class we care about in {@link MyApp}. This class uses field injection:
 * notice how fields are annotated with @{@link Inject}. The injector will automatically set these fields
 * with the singleton of the type. Field injection requires a no-args constructor to be available
 * (any visibility).
 * <p>
 * This class contains a @{@link PostConstruct} method. Such a method is automatically invoked after
 * an object of the class has been instantiated. As you can see, you may use all injected dependencies
 * as they will already have been set when the post construct method is executed.
 */
public class CalculationService {

    private static final BigDecimal TWO_PI = new BigDecimal("6.283185306");

    @Inject
    private Messages messages;

    @Inject
    private RoundingService roundingService;

    private CalculationService() {
    }

    public String calculateCircumference(double d) {
        BigDecimal result = new BigDecimal(d).multiply(TWO_PI);
        String message = messages.getCalculationResultMessage();

        return message + " " + roundingService.round(result);
    }

    @PostConstruct
    private void outputCreationMessage() {
        System.out.println(messages.getCreatedServiceMsg());
    }
}
