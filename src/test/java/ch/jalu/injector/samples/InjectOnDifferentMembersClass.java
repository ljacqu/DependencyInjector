package ch.jalu.injector.samples;

import javax.inject.Inject;

/**
 * Sample class with @Inject on different members.
 */
public class InjectOnDifferentMembersClass {

    @Inject
    private GammaService gammaService;

    @Inject
    private BetaManager betaManager;

    InjectOnDifferentMembersClass() {
        // Field injection requires no-args constructor
    }

    @Inject
    InjectOnDifferentMembersClass(GammaService gammaService, ProvidedClass providedClass) {
        // noop
    }

}
