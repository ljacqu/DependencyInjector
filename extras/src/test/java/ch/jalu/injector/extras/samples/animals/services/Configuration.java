package ch.jalu.injector.extras.samples.animals.services;

import ch.jalu.injector.extras.handlers.testimplementations.ProfilePostConstructHandler;

/**
 * General configuration.
 */
public class Configuration implements BasicService {

    private String lang;

    public void setLang(String lang) {
        this.lang = lang;
    }

    @ProfilePostConstructHandler.Profile
    public String getLang() {
        return lang;
    }
}
