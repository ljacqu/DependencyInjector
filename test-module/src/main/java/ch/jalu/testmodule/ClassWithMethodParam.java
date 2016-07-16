package ch.jalu.testmodule;

import ch.jalu.injector.annotations.NoMethodScan;
import ch.jalu.testmodule.services.TwoService;

import javax.inject.Inject;
import javax.validation.Validator;

/**
 * Sample class with a method param from a non-provided dependency.
 */
@NoMethodScan
public class ClassWithMethodParam {

    @Inject
    private TwoService twoService;

    ClassWithMethodParam() {
    }

    public String getName() {
        return twoService.getName();
    }

    private void processValidator(Validator validator) {
        // noop
    }
}
