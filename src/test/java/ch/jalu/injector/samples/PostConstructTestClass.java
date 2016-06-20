package ch.jalu.injector.samples;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sample class for testing the execution of @PostConstruct methods.
 */
public class PostConstructTestClass {

    @Inject
    @Size
    private int size;
    @Inject
    private BetaManager betaManager;
    private boolean wasPostConstructCalled = false;

    @PostConstruct
    public void postConstructMethod() {
        wasPostConstructCalled = true;
    }

    public boolean wasPostConstructCalled() {
        return wasPostConstructCalled;
    }

    public BetaManager getBetaManager() {
        return betaManager;
    }

}
