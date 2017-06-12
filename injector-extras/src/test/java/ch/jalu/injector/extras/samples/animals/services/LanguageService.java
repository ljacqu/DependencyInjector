package ch.jalu.injector.extras.samples.animals.services;

import javax.inject.Inject;

/**
 * Language service (translations).
 */
public class LanguageService {

    @Inject
    private Configuration configuration;

    public String translate(String name) {
        if ("de".equals(configuration.getLang())) {
            return getDeTranslation(name);
        }
        return name;
    }

    private String getDeTranslation(String name) {
        switch (name) {
            case "Sparrow": return "Spatz";
            case "Ostrich": return "Strauss";
            case "Frog": return "Frosch";
            case "Lion": return "Löwe";
            case "Turtle": return "Schildkröte";
            default: return name;
        }
    }
}
