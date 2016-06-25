package ch.jalu.injector.samples.animals.services;

public interface SoundService {

    String makeSound();

    boolean isMuted();

    void setMute(boolean isMute);
}
