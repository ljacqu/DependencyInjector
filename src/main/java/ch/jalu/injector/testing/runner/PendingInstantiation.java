package ch.jalu.injector.testing.runner;

import ch.jalu.injector.instantiation.Instantiation;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.lang.reflect.Field;

/**
 * Contains an instantiation and the field it's for.
 */
@Getter
@AllArgsConstructor
class PendingInstantiation {

    private final Field field;
    private final Instantiation<?> instantiation;

}
