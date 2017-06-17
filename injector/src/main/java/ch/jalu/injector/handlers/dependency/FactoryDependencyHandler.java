package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.UnresolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.factory.Factory;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Instantiation;
import ch.jalu.injector.handlers.instantiation.SimpleObjectResolution;
import ch.jalu.injector.utils.ReflectionUtils;

/**
 * Dependency handler that builds {@link Factory} objects.
 */
public class FactoryDependencyHandler implements Handler {

    @Override
    public Instantiation<?> get(UnresolvedContext context) {
        final Class<?> clazz = context.getIdentifier().getTypeAsClass();
        if (Factory.class.equals(clazz)) {
            Class<?> genericType = ReflectionUtils.getGenericType(context.getIdentifier().getType());
            if (genericType == null) {
                throw new InjectorException("Factory fields must have concrete generic type. "
                    + "Cannot get generic type for field in '" + context.getIdentifier().getTypeAsClass() + "'");
            }

            return new SimpleObjectResolution<>(new FactoryImpl<>(genericType, context.getInjector()));
        }
        return null;
    }

    private static final class FactoryImpl<P> implements Factory<P> {

        private final Injector injector;
        private final Class<P> parentClass;

        FactoryImpl(Class<P> parentClass, Injector injector) {
            this.parentClass = parentClass;
            this.injector = injector;
        }

        @Override
        public <C extends P> C newInstance(Class<C> clazz) {
            if (parentClass.isAssignableFrom(clazz)) {
                return injector.newInstance(clazz);
            }
            throw new InjectorException(clazz + " not child of " + parentClass);
        }
    }
}
