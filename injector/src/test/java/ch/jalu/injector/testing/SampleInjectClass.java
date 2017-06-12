package ch.jalu.injector.testing;

import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.ClassWithAbstractDependency;
import ch.jalu.injector.samples.ProvidedClass;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

/**
 * Sample class for testing {@link InjectDelayed}.
 */
public class SampleInjectClass {

    @Inject
    private AlphaService alphaService;

    @Inject
    private ClassWithAbstractDependency.AbstractDependency abstractDependency;

    @Inject
    @CustomAnnotation
    private String stringField;

    private ProvidedClass providedClass;


    @PostConstruct
    private void postConstruct() {
        providedClass = alphaService.getProvidedClass();
    }


    // -- Trivial getters

    public AlphaService getAlphaService() {
        return alphaService;
    }

    public ClassWithAbstractDependency.AbstractDependency getAbstractDependency() {
        return abstractDependency;
    }

    public ProvidedClass getProvidedClass() {
        return providedClass;
    }

    public String getStringField() {
        return stringField;
    }

}
