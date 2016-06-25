package ch.jalu.injector.samples.animals.services;

import ch.jalu.injector.samples.animals.Animal;

import javax.inject.Inject;

/**
 * Constructs the name of animals based on their class.
 */
public class NameService {

    @Inject
    private LanguageService languageService;

    public String constructName(Animal animal) {
        return languageService.translate(animal.getClass().getSimpleName());
    }

}
