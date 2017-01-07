package ch.jalu.injector.samples.inheritance;

import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sample child class.
 */
public class Child extends Parent {

    private final GammaService gammaService;
    private boolean isPostConstructRunAfterParent;

    @Inject
    Child(ProvidedClass providedClass, GammaService gammaService) {
        super(providedClass);
        this.gammaService = gammaService;
    }

    @PostConstruct
    private void initChild() {
        isPostConstructRunAfterParent = parentPostConstructRun;
    }

    public boolean isPostConstructRunAfterParent() {
        return isPostConstructRunAfterParent;
    }

    public GammaService getChildGammaService() {
        return gammaService;
    }

}
