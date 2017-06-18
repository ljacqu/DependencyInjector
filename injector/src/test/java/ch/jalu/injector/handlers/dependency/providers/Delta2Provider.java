package ch.jalu.injector.handlers.dependency.providers;

import javax.inject.Provider;

/**
 * Provider for {@link Delta2}.
 */
public class Delta2Provider implements Provider<Delta2> {

    private final Charlie charlie;

    public Delta2Provider(Charlie charlie) {
        this.charlie = charlie;
    }

    @Override
    public Delta2 get() {
        Delta2 delta2 = new Delta2("pre_");
        delta2.setCharlie(charlie);
        return delta2;
    }
}
