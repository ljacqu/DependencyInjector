package ch.jalu.injector.samples;

import lombok.Getter;

import javax.inject.Inject;

/**
 * Test with an abstract class declared as dependency.
 */
@Getter
public class ClassWithAbstractDependency {

    private final AlphaService alphaService;
    private final AbstractDependency abstractDependency;

    @Inject
    public ClassWithAbstractDependency(AlphaService as, AbstractDependency ad) {
        this.alphaService = as;
        this.abstractDependency = ad;
    }

    public static abstract class AbstractDependency {
    }

    public static final class ConcreteDependency extends AbstractDependency {
    }
}
