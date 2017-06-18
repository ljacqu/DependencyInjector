package ch.jalu.injector.handlers.dependency.providers;

import javax.inject.Inject;
import javax.inject.Provider;

/**
 * Provider for {@link Delta1}.
 */
public class Delta1Provider implements Provider<Delta1> {

    @Inject
    private Bravo bravo;

    @Override
    public Delta1 get() {
        return new Delta1(bravo, 19);
    }

}
