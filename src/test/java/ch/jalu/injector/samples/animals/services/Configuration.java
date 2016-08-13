package ch.jalu.injector.samples.animals.services;

import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;

/**
 * General configuration.
 */
public class Configuration {

    private String lang;

    public void setLang(String lang) {
        this.lang = lang;
    }

    @ProfilePostConstructHandler.Profile
    public String getLang() {
        return lang;
    }
}
