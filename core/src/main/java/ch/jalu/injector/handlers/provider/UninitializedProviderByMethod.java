package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;

import javax.inject.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Instantiation method for a provider method whose arguments have not been collected yet.
 * <p>
 * The arguments the method takes are declared as dependencies, i.e. in order to create
 * a provider based on calls to the given method we first require the arguments the method
 * takes.
 *
 * @param <T> the type the provider method produces
 */
public class UninitializedProviderByMethod<T> implements Instantiation<Provider<T>> {

    private final Method method;
    private final Object instance;

    public UninitializedProviderByMethod(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        Class<?>[] parameters = method.getParameterTypes();
        Type[] genericTypes = method.getGenericParameterTypes();
        Annotation[][] annotations = method.getParameterAnnotations();

        List<DependencyDescription> dependencies = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; ++i) {
            dependencies.add(new DependencyDescription(parameters[i], genericTypes[i], annotations[i]));
        }
        return dependencies;
    }

    @Override
    public Provider<T> instantiateWith(Object... values) {
        return new ProviderByMethod<>(method, instance, values);
    }
}
