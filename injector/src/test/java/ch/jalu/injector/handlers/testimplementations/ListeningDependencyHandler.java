package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;

/**
 * Sample annotation handler implementation that simply counts how many times it was invoked.
 */
public class ListeningDependencyHandler extends AbstractCountingHandler implements DependencyHandler {

    @Override
    public Object resolveValue(ResolvedInstantiationContext<?> context, DependencyDescription dependencyDescription) {
        increment();
        return null;
    }
}
