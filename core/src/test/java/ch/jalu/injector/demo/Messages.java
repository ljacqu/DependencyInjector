package ch.jalu.injector.demo;

import javax.inject.Inject;

/**
 * Manages localized messages.
 * <p>
 * This demo class uses constructor injection: notice the @Inject annotation on
 * the constructor. The constructor doesn't have to be public; typically it is
 * package-private as to prevent "accidental" initialization from the outside,
 * while still allowing it to be used for unit testing.
 */
public class Messages {

    private final String[] messages;

    @Inject
    Messages(Settings settings) {
        messages = initMessages(settings.getLanguage());
    }

    public String getCreatedServiceMsg() {
        return messages[0];
    }

    public String getCalculationResultMessage() {
        return messages[1];
    }

    private static String[] initMessages(String language) {
        if ("fr".equals(language)) {
            return new String[]{"Service créé avec succès", "Résultat de la calculation :"};
        } else if ("gsw".equals(language)) {
            return new String[]{"Service erfolgriich erstellt", "Resultat vo de Berechnig:"};
        } else {
            return new String[]{"Created service successfully", "Result of the calculation:"};
        }
    }
}
