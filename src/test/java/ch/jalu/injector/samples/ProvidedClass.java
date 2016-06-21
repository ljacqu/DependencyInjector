package ch.jalu.injector.samples;

import lombok.Getter;

import javax.inject.Inject;

/**
 * Sample - class that is always provided to the initializer beforehand.
 */
@Getter
public class ProvidedClass implements Reloadable {

    private boolean wasReloadCalled;

    @Inject
    public ProvidedClass() {
        throw new IllegalStateException("Should never be called (tests always provide this class)");
    }

    public ProvidedClass(String manualConstructor) {
    }

}
