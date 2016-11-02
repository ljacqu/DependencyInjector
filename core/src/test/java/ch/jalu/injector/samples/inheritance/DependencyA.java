package ch.jalu.injector.samples.inheritance;


import javax.inject.Inject;

public class DependencyA {

    @Inject
    DependencyA(DependencyB b) {
    }
}
