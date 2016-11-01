package ch.jalu.injector.extras.samples.animals.services;

/**
 * For frogs.
 */
public class CroakService implements SoundService {

    private boolean isMuted;

    @Override
    public String makeSound() {
        return isMuted ? null : "Croak, croak";
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
