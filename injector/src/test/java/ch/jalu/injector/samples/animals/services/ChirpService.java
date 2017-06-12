package ch.jalu.injector.samples.animals.services;

public class ChirpService implements SoundService {

    private boolean isMuted = false;

    @Override
    public String makeSound() {
        return isMuted ? null : "Chirp!";
    }

    @Override
    public boolean isMuted() {
        return isMuted;
    }

    public void setMute(boolean isMute) {
        this.isMuted = isMute;
    }
}
