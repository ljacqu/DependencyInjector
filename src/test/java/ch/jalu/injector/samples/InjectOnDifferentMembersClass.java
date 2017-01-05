package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class with @Inject on different members.
 */
public class InjectOnDifferentMembersClass {

    private final ProvidedClass providedClass;

    @Inject
    private GammaService gammaService;

    @Inject
    private BetaManager betaManager;

    InjectOnDifferentMembersClass() {
        this.providedClass = null;
    }

    @Inject
    InjectOnDifferentMembersClass(ProvidedClass providedClass) {
        this.providedClass = providedClass;
    }
}
