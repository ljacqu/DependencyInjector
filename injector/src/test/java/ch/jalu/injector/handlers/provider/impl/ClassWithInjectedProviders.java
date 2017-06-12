package ch.jalu.injector.handlers.provider.impl;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample class with provider dependencies.
 */
public class ClassWithInjectedProviders {

    @Inject
    private Provider<Charlie> charlieProvider;
    @Inject
    private Bravo bravo;
    @Inject
    private Provider<Delta> deltaProvider;

    protected ClassWithInjectedProviders() {
    }

    public List<Charlie> charlieList() {
        return listWithSize(charlieProvider, 3);
    }

    public List<Delta> deltaList() {
        return listWithSize(deltaProvider, 2);
    }

    public Provider<Charlie> getCharlieProvider() {
        return charlieProvider;
    }
    public Provider<Delta> getDeltaProvider() {
        return deltaProvider;
    }
    public Bravo getBravo() {
        return bravo;
    }

    private <T> List<T> listWithSize(Provider<T> provider, int size) {
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; ++i) {
            list.add(provider.get());
        }
        return list;
    }
}
