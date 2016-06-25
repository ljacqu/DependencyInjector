package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.Injector;
import ch.jalu.injector.handlers.annotations.AnnotationHandler;
import ch.jalu.injector.instantiation.DependencyDescription;

/**
 * Sample annotation handler implementation that simply counts how many times it was invoked.
 */
public class ListeningAnnotationHandler extends AbstractCountingHandler implements AnnotationHandler {

    @Override
    public Object resolveValue(Injector injector, DependencyDescription dependencyDescription) {
        increment();
        return null;
    }
}
