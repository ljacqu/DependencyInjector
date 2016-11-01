package ch.jalu.injector.samples.inheritance;

import javax.inject.Provider;


public class DependencyBProvider implements Provider<DependencyB> {
    @Override
    public DependencyB get() {
        return DependencyB.create();
    }
}
