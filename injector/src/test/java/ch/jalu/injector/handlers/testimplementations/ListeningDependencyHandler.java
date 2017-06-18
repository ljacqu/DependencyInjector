package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;

/**
 * Sample handler implementation that simply counts how many times it was invoked.
 */
public class ListeningDependencyHandler extends AbstractCountingHandler implements Handler {

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        increment(context);
        return null;
    }
}
