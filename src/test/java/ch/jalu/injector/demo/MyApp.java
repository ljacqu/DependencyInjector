package ch.jalu.injector.demo;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.handlers.dependency.SavedAnnotationsHandler;

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
        // Create the handler that allows custom annotations, see @RoundMode and RoundingService
        SavedAnnotationsHandler savedAnnotationsHandler = new SavedAnnotationsHandler();
        // Save the value to associate to @RoundMode, our custom annotation
        savedAnnotationsHandler.register(RoundMode.class, RoundingMode.HALF_UP);

        // Create the injector via the Builder.
        // Notice how we pass SavedAnnotationsHandler first -> we want the injector to pass through this one before
        // the default handlers
        Injector injector = new InjectorBuilder()
                .addHandlers(savedAnnotationsHandler)
                .addDefaultHandlers("ch.jalu.injector.demo")
                .create();

        // Tell the injector that we want to get CalculationService
        CalculationService calculationService = injector.getSingleton(CalculationService.class);

        // Run an action on it
        return calculationService.calculateCircumference(120.12345);
    }

    // Implementation note: it may seem confusing that the SavedAnnotationHandler has to be instantiated and passed
    // separately; this is due to the fact that you would not be able to register any annotations without being able to
    // interact with that handler. The alternative would have been to add such a method on the Injector interface, but
    // this forces the injector implementation to use an annotation handler. We want to avoid this as to allow users
    // of the injector to remove it and/or replace it with other handlers.
    //
    // Nevertheless, this is still quite inelegant and may be subject to change in future versions, e.g. by making the
    // injector propagate a method's parameters to the handlers of a given type.
}
