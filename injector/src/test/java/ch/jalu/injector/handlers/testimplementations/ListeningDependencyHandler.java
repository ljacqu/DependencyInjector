package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;

/**
 * Sample handler implementation that simply counts how many times it was invoked.
 */
public class ListeningDependencyHandler extends AbstractCountingHandler implements Handler {

    @Override
    public Instantiation<?> get(UnresolvedContext context) {
        increment();
        return null;
    }
}
