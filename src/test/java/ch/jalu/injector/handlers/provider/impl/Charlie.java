package ch.jalu.injector.handlers.provider.impl;

import javax.inject.Inject;

/**
 * Test class.
 */
public class Charlie {

    @Inject
    private Alfa alfa;

    @Inject
    private Bravo bravo;

    Charlie() {
    }

    public String getString() {
        return bravo.getServiceName() + alfa.transform("_Charlie");
    }
}
