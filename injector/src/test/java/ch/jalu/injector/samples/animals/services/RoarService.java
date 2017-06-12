package ch.jalu.injector.samples.animals.services;

public class RoarService implements SoundService {

    @Override
    public String makeSound() {
        return "Roar";
    }

    @Override
    public void setMute(boolean isMuted) {
        // Animals that roar cannot be muted
    }

    @Override
    public boolean isMuted() {
        return false;
    }

}
