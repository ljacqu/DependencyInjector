package ch.jalu.injector.extras.samples.animals;

import ch.jalu.injector.extras.samples.animals.services.Configuration;
import ch.jalu.injector.extras.samples.animals.services.HissService;
import ch.jalu.injector.extras.samples.animals.services.SoundService;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Snake.
 */
public class Snake extends Reptile {

    private final SoundService soundService;
    private final Configuration configuration;
    private String sound;

    @Inject
    private Snake(HissService hissService, Configuration configuration) {
        this.soundService = hissService;
        this.configuration = configuration;
    }

    public String makeSound() {
        return soundService.isMuted() ? null : sound;
    }

    @Override
    public String getName() {
        return "de".equals(configuration.getLang())
            ? "Schlange" : "snake";
    }

    @PostConstruct
    private void saveSound() {
        sound = soundService.makeSound();
    }
}
