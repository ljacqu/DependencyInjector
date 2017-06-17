package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.factory.SingletonStore;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.SimpleObjectResolution;
import ch.jalu.injector.utils.ReflectionUtils;

import java.util.Collection;

/**
 * Dependency handler that builds {@link SingletonStore} objects.
 */
public class SingletonStoreDependencyHandler implements Handler {

    @Override
    public Instantiation<?> get(UnresolvedContext context) {
        if (SingletonStore.class.equals(context.getIdentifier().getTypeAsClass())) {
            Class<?> genericType = ReflectionUtils.getGenericType(context.getIdentifier().getType());
            if (genericType == null) {
                throw new InjectorException("Singleton store fields must have concrete generic type. "
                    + "Cannot get generic type for field in '" + context.getIdentifier().getTypeAsClass() + "'");
            }

            return new SimpleObjectResolution<>(new SingletonStoreImpl<>(genericType, context.getInjector()));
        }
        return null;
    }

    private static final class SingletonStoreImpl<P> implements SingletonStore<P> {

        private final Injector injector;
        private final Class<P> parentClass;

        SingletonStoreImpl(Class<P> parentClass, Injector injector) {
            this.parentClass = parentClass;
            this.injector = injector;
        }

        @Override
        public <C extends P> C getSingleton(Class<C> clazz) {
            if (parentClass.isAssignableFrom(clazz)) {
                return injector.getSingleton(clazz);
            }
            throw new InjectorException(clazz + " not child of " + parentClass);
        }

        @Override
        public Collection<P> retrieveAllOfType() {
            return retrieveAllOfType(parentClass);
        }

        @Override
        public <C extends P> Collection<C> retrieveAllOfType(Class<C> clazz) {
            if (parentClass.isAssignableFrom(clazz)) {
                return injector.retrieveAllOfType(clazz);
            }
            throw new InjectorException(clazz + " not child of " + parentClass);
        }
    }
}
