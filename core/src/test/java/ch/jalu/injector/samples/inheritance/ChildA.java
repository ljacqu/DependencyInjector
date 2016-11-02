package ch.jalu.injector.samples.inheritance;

import javax.inject.Inject;

public class ChildA extends ParentA {

    @Inject
    private DependencyA dp;

}
