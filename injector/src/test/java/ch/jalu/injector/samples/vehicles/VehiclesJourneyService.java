package ch.jalu.injector.samples.vehicles;

import javax.inject.Inject;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Sends vehicles off to a journey.
 */
public class VehiclesJourneyService {

    @Inject
    private Bicycle bicycle;
    @Inject
    private Car car;
    @Inject
    private Plane plane;
    @Inject
    private Ship ship;

    public void startJourney() {
        startJourney(v -> true);
    }

    public void startJourney(Predicate<Vehicle> pr) {
        Stream.of(bicycle, car, plane, ship)
            .filter(pr).forEach(Vehicle::startJourney);
    }
}
