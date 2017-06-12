package ch.jalu.injector.samples.vehicles.services;


import ch.jalu.injector.samples.AlphaService;

public class SailService implements SteeringService {

    SailService(AlphaService alphaService, int knots) {
    }

    @Override
    public void brake() {

    }

    @Override
    public void steer() {

    }
}
