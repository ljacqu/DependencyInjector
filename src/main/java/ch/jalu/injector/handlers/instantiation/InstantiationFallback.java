package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * Fallback instantiation method for classes with an accessible no-args constructor
 * and no elements whatsoever annotated with {@link Inject} or {@link PostConstruct}.
 */
public class InstantiationFallback<T> implements Instantiation<T> {

    private final Constructor<T> constructor;

    protected InstantiationFallback(Constructor<T> constructor) {
        this.constructor = constructor;
    }

    @Override
    public List<DependencyDescription> getDependencies() {
        return new ArrayList<>();
    }

    @Override
    public T instantiateWith(Object... values) {
        InjectorUtils.checkArgument(values == null || values.length == 0,
                "Instantiation fallback cannot have parameters", constructor.getDeclaringClass());
        return ReflectionUtils.newInstance(constructor);
    }
}
