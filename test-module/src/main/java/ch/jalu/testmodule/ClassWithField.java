package ch.jalu.testmodule;

import ch.jalu.injector.annotations.NoFieldScan;

import javax.validation.Validator;

/**
 * Sample class with a field from a non-provided dependency.
 */
@NoFieldScan
public class ClassWithField {

    private Validator validator;

    public ClassWithField() {
    }

    public String getName(ClassWithMethodParam cwmp) {
        return cwmp.getName();
    }

}
