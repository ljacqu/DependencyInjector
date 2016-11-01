package ch.jalu.injector.samples.inheritance;


public class DependencyB {

    private DependencyB(boolean b) {

    }

    public static DependencyB create() {
        return new DependencyB(false);
    }
}
