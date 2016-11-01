package ch.jalu.injector.extras.samples.animals.services;

import javax.inject.Inject;

/**
 * Squeaky squeak.
 */
public class SqueakService implements SoundService {

    private boolean isMuted;

    @Inject
    private Configuration configuration;

    @Override
    public String makeSound() {
        if (isMuted) {
            return null;
        }
        return "de".equals(configuration.getLang()) ? "krächz" : "squeak";
    }

    @Override
    public boolean isMuted() {
        return isMuted;
    }

    @Override
    public void setMute(boolean isMute) {
        this.isMuted = isMute;
    }
}
