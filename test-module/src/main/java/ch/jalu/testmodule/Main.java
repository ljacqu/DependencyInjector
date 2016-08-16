package ch.jalu.testmodule;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.testmodule.services.providers.FiveService;
import ch.jalu.testmodule.services.providers.FiveServiceProvider;
import ch.jalu.testmodule.services.providers.FiveServiceProviderProvider;
import ch.jalu.testmodule.services.providers.FourService;
import ch.jalu.testmodule.services.providers.FourServiceProvider;

import javax.validation.Validation;

public class Main {

    public static void main(String... args) {
        runOptionalDependencyTest();
        runProviderTest();
    }

    private static void runOptionalDependencyTest() {
        // Initially check that class from provided-scoped dependency is NOT present
        try {
            new Validation();
            throw new IllegalStateException("Expected Validation not to be in class path!");
        } catch (NoClassDefFoundError e) {
            // all good
        }

        // Create injector
        Injector injector = new InjectorBuilder().addDefaultHandlers("ch.jalu.testmodule").create();

        // Instantiate class with method param
        ClassWithMethodParam classWithMethodParam = injector.getSingleton(ClassWithMethodParam.class);
        if (!"one service".equals(classWithMethodParam.getName())) {
            throw new IllegalStateException("ClassWithMethodParam#getName was not as expected");
        }

        // Instantiate class with optional field (via fallback instantiation to trigger field injection scan)
        ClassWithField classWithField = injector.getSingleton(ClassWithField.class);
        if (!"one service".equals(classWithField.getName(classWithMethodParam))) {
            throw new IllegalStateException("ClassWithField#getName was not as expected");
        }
    }

    private static void runProviderTest() {
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers("ch.jalu.testmodule.services")
            .create();
        injector.registerProvider(FiveService.class, FiveServiceProvider.class);
        injector.registerProvider(FiveServiceProvider.class, FiveServiceProviderProvider.class);
        injector.registerProvider(FourService.class, new FourServiceProvider(injector));

        FiveService fiveService = injector.getSingleton(FiveService.class);
        if (!"one service".equals(fiveService.getName())) {
            throw new IllegalStateException("FiveService#getName was not as expected");
        }
    }
}