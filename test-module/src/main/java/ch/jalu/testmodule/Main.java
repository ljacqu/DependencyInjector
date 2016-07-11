package ch.jalu.testmodule;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;

import javax.validation.Validation;

public class Main {

    public static void main(String... args) {
        // Initially check that class from provided-scoped dependency is NOT present
        try {
            new Validation();
            throw new IllegalStateException("Expected Validation not to be in class path!");
        } catch (NoClassDefFoundError e) {
            // all good
        }

        Injector injector = new InjectorBuilder().addDefaultHandlers("ch.jalu.testmodule").create();
        ClassWithMethodParam classWithMethodParam = injector.getSingleton(ClassWithMethodParam.class);
        if (!"one service".equals(classWithMethodParam.getName())) {
            throw new IllegalStateException("ClassWithMethodParam#getName was not as expected");
        }

    }
}