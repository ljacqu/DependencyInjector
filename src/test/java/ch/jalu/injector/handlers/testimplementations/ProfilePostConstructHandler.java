package ch.jalu.injector.handlers.testimplementations;

import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.handlers.instantiation.ConstructorInjection;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import javassist.util.proxy.MethodFilter;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Sample post construct handler that wraps a constructed class into a proxy
 * for logging purposes when methods annotated with {@link Profile} are found.
 */
public class ProfilePostConstructHandler implements PostConstructHandler {

    private static final MethodFilter METHOD_FILTER = new MethodFilter() {
        @Override
        public boolean isHandled(Method m) {
            return m.isAnnotationPresent(Profile.class);
        }
    };

    private final InjectorImpl injector;
    private final List<String> invocations = new ArrayList<>();

    public ProfilePostConstructHandler(InjectorImpl injector) {
        this.injector = injector;
    }

    @Override
    public <T> T process(final T object) throws ReflectiveOperationException {
        final Class<?> clazz = object.getClass();
        if (!hasProfileMethod(clazz)) {
            return null;
        }

        // The Proxy generated by javassist will have the same constructor args as on the real class
        // We can easily check and satisfy this by getting the instantiation method: if it is constructor injection
        // get the args from it. This approach requires some refactoring to support custom instantiation methods.
        Instantiation<?> instantiation = getInstantiation(clazz);
        Class<?>[] constructorTypes = instantiation instanceof ConstructorInjection<?>
            ? getConstructorType((ConstructorInjection<?>) instantiation)
            : new Class<?>[0];
        Object[] constructorArgs = resolveConstructorArgs(constructorTypes);

        ProxyFactory pf = new ProxyFactory();
        pf.setSuperclass(clazz);
        Class<?> proxyClass = pf.createClass(METHOD_FILTER);

        T proxy = (T) proxyClass
            .getConstructor(constructorTypes)
            .newInstance(constructorArgs);

        ((Proxy) proxy).setHandler(new MethodHandler() {
            @Override
            public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args)
                                 throws ReflectiveOperationException {
                invocations.add(clazz.getSimpleName() + "#" + thisMethod.getName());
                return thisMethod.invoke(object, args);
            }
        });
        return proxy;
    }

    public List<String> getInvocations() {
        return invocations;
    }

    private <T> Instantiation<T> getInstantiation(Class<T> clazz) {
        for (InstantiationProvider provider : injector.getConfig().getInstantiationProviders()) {
            Instantiation<T> instantiation = provider.get(clazz);
            if (instantiation != null) {
                return instantiation;
            }
        }
        throw new IllegalStateException("Could not get instantiation for '" + clazz + "': are the instantiation "
            + "methods not in sync with the injector's?");
    }

    private static Class<?>[] getConstructorType(ConstructorInjection<?> injection) {
        Class<?>[] classes = new Class<?>[injection.getDependencies().size()];
        int i = 0;
        for (DependencyDescription description : injection.getDependencies()) {
            classes[i] = description.getType();
            ++i;
        }
        return classes;
    }

    private Object[] resolveConstructorArgs(Class<?>[] classes) {
        List<Object> list = new ArrayList<>(classes.length);
        for (Class<?> clazz : classes) {
            list.add(injector.getIfAvailable(clazz));
        }
        return list.toArray();
    }

    private static boolean hasProfileMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (METHOD_FILTER.isHandled(method)) {
                return true;
            }
        }
        return false;
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Profile {

    }
}
