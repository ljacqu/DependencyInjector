package ch.jalu.injector.handlers.provider;

import ch.jalu.injector.utils.InjectorUtils;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.inject.Provider;
import java.lang.reflect.Method;

/**
 * Wraps a method call into a Provider type.
 */
public class ProviderByMethod<T> implements Provider<T> {

    private final Method method;
    private final Object instance;
    private final Object[] arguments;

    /**
     * Constructor.
     *
     * @param method the method to call
     * @param instance the instance to call the method on
     * @param arguments the arguments to pass to the method
     */
    public ProviderByMethod(Method method, Object instance, Object... arguments) {
        InjectorUtils.checkArgument(method.getParameterTypes().length == 0,
                "Provider method may not take any arguments");
        this.method = method;
        this.instance = instance;
        this.arguments = arguments;
    }

    @Override
    public T get() {
        return (T) ReflectionUtils.invokeMethod(method, instance, arguments);
    }
}
