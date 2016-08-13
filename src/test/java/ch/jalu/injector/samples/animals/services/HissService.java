package ch.jalu.injector.samples.animals.services;

import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;

/**
 * Service for hissing.
 */
public class HissService implements SoundService {

    private boolean isMuted;

    @Override
    @ProfilePostConstructHandler.Profile
    public String makeSound() {
        return "Hiss";
    }

    @Override
    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void setMute(boolean isMuted) {
        this.isMuted = isMuted;
    }

}
