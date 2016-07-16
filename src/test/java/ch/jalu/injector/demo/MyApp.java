package ch.jalu.injector.demo;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;

import java.math.RoundingMode;

/**
 * Small demo showing how an injector can be used to wire up an application.
 * <p>
 * In this entry point of the app, we only care about getting {@link CalculationService}.
 * We don't care and don't have to care about creating any of the classes it depends on
 * by creating the injector and letting it figure out which classes to create on its own.
 */
public class MyApp {

    public static void main(String... args) {
        String result = setUpAndRun();
        System.out.println(result);
    }

    public static String setUpAndRun() {
        // Create the injector via the Builder
        Injector injector = new InjectorBuilder()
                .addDefaultHandlers("ch.jalu.injector.demo")
                .create();

        // Save the value to associate to @RoundMode, our custom annotation
        injector.provide(RoundMode.class, RoundingMode.HALF_UP);

        // Tell the injector that we want to get CalculationService
        CalculationService calculationService = injector.getSingleton(CalculationService.class);

        // Run an action on it
        return calculationService.calculateCircumference(120.12345);
    }
}
