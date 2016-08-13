package ch.jalu.injector.samples.animals.services;

import ch.jalu.injector.annotations.AllInstances;
import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;

import javax.inject.Inject;

/**
 * Manages all sound service implementations.
 */
public class SoundServiceSupervisor {

    @Inject
    private Configuration configuration;

    @Inject
    @AllInstances
    private SoundService[] soundServices;

    // true if successful, false otherwise
    @ProfilePostConstructHandler.Profile
    public boolean muteAll() {
        if ("en".equals(configuration.getLang())) {
            for (SoundService soundService : soundServices) {
                soundService.setMute(true);
            }
            return true;
        }
        // muting all only possible in English -> return false
        return false;
    }

    public SoundService[] getSoundServices() {
        return soundServices;
    }

}
