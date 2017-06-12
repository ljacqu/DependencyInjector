package ch.jalu.injector.samples.animals.services;

/**
 * Service for hissing.
 */
public class HissService implements SoundService {

    private boolean isMuted;
    private Configuration configuration;

    public HissService(Configuration configuration) {
        this.configuration = configuration;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
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
