package ch.jalu.injector.testing.runner;

import ch.jalu.injector.instantiation.Instantiation;

import java.lang.reflect.Field;

/**
 * Contains an instantiation and the field it's for.
 */
class PendingInstantiation {

    private final Field field;
    private final Instantiation<?> instantiation;

    public PendingInstantiation(Field field, Instantiation<?> instantiation) {
        this.field = field;
        this.instantiation = instantiation;
    }

    public Field getField() {
        return field;
    }

    public Instantiation<?> getInstantiation() {
        return instantiation;
    }

}
