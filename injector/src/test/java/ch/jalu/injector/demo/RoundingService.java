package ch.jalu.injector.demo;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * Rounds values.
 */
public class RoundingService {

    private final MathContext mathContext;

    @Inject
    RoundingService(Settings settings, @RoundMode RoundingMode roundingMode) {
        this.mathContext = new MathContext(settings.getPrecision(), roundingMode);
    }

    public BigDecimal round(BigDecimal number) {
        return number == null ? null : number.round(mathContext);
    }
}
