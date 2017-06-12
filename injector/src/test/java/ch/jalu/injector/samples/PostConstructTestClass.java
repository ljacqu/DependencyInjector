package ch.jalu.injector.samples;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sample class for testing the execution of @PostConstruct methods.
 */
public class PostConstructTestClass {

    private int size;
    private ProvidedClass providedClass;
    private boolean wasPostConstructCalled = false;

    @Inject
    public PostConstructTestClass(@Size("box") int size, ProvidedClass providedClass) {
        this.providedClass = providedClass;
        this.size = size;
    }

    @PostConstruct
    public void postConstructMethod() {
        wasPostConstructCalled = true;
    }

    public boolean wasPostConstructCalled() {
        return wasPostConstructCalled;
    }

    public ProvidedClass getProvidedClass() {
        return providedClass;
    }

}
