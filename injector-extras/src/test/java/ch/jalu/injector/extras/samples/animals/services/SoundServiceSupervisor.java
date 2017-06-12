package ch.jalu.injector.extras.samples.animals.services;

import ch.jalu.injector.extras.AllInstances;

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
