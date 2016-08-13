package ch.jalu.injector.handlers.provider.impl;

import javax.inject.Inject;

/**
 * Test class.
 */
public class Bravo {

    private Alfa alfa;

    @Inject
    Bravo(Alfa alfa) {
        this.alfa = alfa;
    }

    public String getServiceName() {
        return alfa.transform("Bravo");
    }
}
