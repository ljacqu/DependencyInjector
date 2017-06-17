package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.context.ResolvedContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.factory.Factory;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.utils.ReflectionUtils;

/**
 * Dependency handler that builds {@link Factory} objects.
 */
public class FactoryDependencyHandler implements Handler {

    @Override
    public Object resolveValue(ResolvedContext context, DependencyDescription dependencyDescription) {
        if (Factory.class.equals(dependencyDescription.getTypeAsClass())) {
            Class<?> genericType = ReflectionUtils.getGenericType(dependencyDescription.getType());
            if (genericType == null) {
                throw new InjectorException("Factory fields must have concrete generic type. "
                    + "Cannot get generic type for field in '" + context.getIdentifier().getType() + "'");
            }

            return new FactoryImpl<>(genericType, context.getInjector());
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
