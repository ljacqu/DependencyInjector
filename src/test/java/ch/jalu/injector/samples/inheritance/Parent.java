package ch.jalu.injector.samples.inheritance;

import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Parent.
 */
public class Parent extends Grandparent {

    private final ProvidedClass providedClass;
    protected boolean parentPostConstructRun;

    @Inject
    private BetaManager betaManager;

    @Inject
    Parent(ProvidedClass providedClass) {
        this.providedClass = providedClass;
    }

    @PostConstruct
    private void init() {
        parentPostConstructRun = true;
    }

    public ProvidedClass getParentProvidedClass() {
        return providedClass;
    }

    public BetaManager getParentBetaManager() {
        return betaManager;
    }
}
