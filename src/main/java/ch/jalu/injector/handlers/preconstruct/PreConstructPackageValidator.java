package ch.jalu.injector.handlers.preconstruct;

import ch.jalu.injector.exceptions.InjectorException;

/**
 * Validates the package of a parameter type to ensure that it is part of the allowed packages.
 * This ensures that we don't try to instantiate things that are beyond our reach in case some
 * external dependency has not been registered by accident.
 */
public class PreConstructPackageValidator extends PlainPreConstructHandler {

    private final String rootPackage;

    /**
     * Constructor.
     *
     * @param rootPackage the main package under which classes may be instantiated
     */
    public PreConstructPackageValidator(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    @Override
    public void process(Class<?> clazz) {
        if (clazz.getPackage() == null) {
            if (clazz.isPrimitive()) {
                throw new InjectorException("Cannot instantiate '" + clazz + "'. Primitive types must be provided"
                    + " explicitly (or use an annotation).");
            } else if (clazz.isArray()) {
                throw new InjectorException("Found array class '" + clazz + "'. Unknown how to inject (did you forget"
                    + " to add a custom handler?).");
            } else {
                throw new InjectorException("Unknown class '" + clazz + "'.");
            }
        }
        String packageName = clazz.getPackage().getName();
        if (!packageName.startsWith(rootPackage)) {
            throw new InjectorException("Class '" + clazz + "' with package '" + packageName + "' is outside of the "
                + "allowed packages. It must be provided explicitly or the package must be passed to the constructor.");
        }
    }

}
