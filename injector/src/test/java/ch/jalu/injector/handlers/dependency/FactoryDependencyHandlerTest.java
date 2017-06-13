package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.factory.Factory;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.StandardInjectionProvider;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.inheritance.Child;
import ch.jalu.injector.samples.inheritance.Grandparent;
import ch.jalu.injector.samples.inheritance.Parent;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import static ch.jalu.injector.TestUtils.findOrThrow;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link FactoryDependencyHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class FactoryDependencyHandlerTest {

    private Injector injector;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Before
    public void setUpInjector() {
        injector = new InjectorBuilder().addHandlers(createHandlers()).create();
        injector.register(ProvidedClass.class, new ProvidedClass(""));
    }

    @Test
    public void shouldCreateGrandparentTypes() {
        // given
        Factory<Grandparent> factory = getFactoryForClass(Grandparent.class);

        // when
        Grandparent grandparent = factory.newInstance(Grandparent.class);
        Child child = factory.newInstance(Child.class);
        Child child2 = factory.newInstance(Child.class);

        // then
        assertThat(grandparent, not(nullValue()));
        assertThat(child, not(nullValue()));
        assertThat(child2, not(nullValue()));
        assertThat(child, not(sameInstance(child2)));
    }

    @Test
    public void shouldThrowForClassNotWithinBounds() {
        // given
        Factory<Parent> factory = getFactoryForClass(Parent.class);

        // expect
        exceptionCatcher.expect("not child of " + Parent.class);

        // when
        factory.newInstance((Class) Grandparent.class);
    }

    @Test
    public void shouldAllowObjectAsClass() {
        // given
        Factory<Object> factory = getFactoryForClass(Object.class);

        // when
        Grandparent grandparent = factory.newInstance(Grandparent.class);
        BetaManager betaManager = factory.newInstance(BetaManager.class);

        // then
        assertThat(grandparent, not(nullValue()));
        assertThat(betaManager, not(nullValue()));
        assertThat(betaManager, not(sameInstance(injector.getSingleton(BetaManager.class))));
    }

    @Test
    public void shouldThrowForUnspecifiedGenerics() {
        // given
        ResolvedInstantiationContext<Object> context = new ResolvedInstantiationContext<>(
            injector, StandardResolutionType.SINGLETON, Object.class, Object.class, null);
        DependencyDescription description = new DependencyDescription(Factory.class, null);

        // expect
        exceptionCatcher.expect("Factory fields must have concrete generic type.");

        // when
        getFactoryHandler().resolveValue(context, description);
    }

    @Test
    public void shouldReturnNullForNonFactoryType() {
        // given
        ResolvedInstantiationContext<Object> context = new ResolvedInstantiationContext<>(
            injector, StandardResolutionType.SINGLETON, Object.class, Object.class, null);
        DependencyDescription description = new DependencyDescription(Parent.class, null);

        // when
        Object result = getFactoryHandler().resolveValue(context, description);

        // then
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unchecked")
    private <T> Factory<T> getFactoryForClass(Class<T> clazz) {
        FactoryDependencyHandler factoryHandler = getFactoryHandler();
        return (Factory<T>) factoryHandler.resolveValue(
            new ResolvedInstantiationContext<>(injector, StandardResolutionType.SINGLETON,
                Object.class, Object.class, null),
            new DependencyDescription(createParameterizedType(Factory.class, clazz), null)
        );
    }

    private FactoryDependencyHandler getFactoryHandler() {
        List<Handler> dependencyHandlers = ((InjectorImpl) injector).getConfig().getHandlers();
        Handler factoryHandler = findOrThrow(dependencyHandlers, handler -> handler instanceof FactoryDependencyHandler);
        return (FactoryDependencyHandler) factoryHandler;
    }

    private static ParameterizedType createParameterizedType(Type rawType, Type... actualTypeArguments) {
        return new ParameterizedType() {
            @Override
            public Type[] getActualTypeArguments() {
                return actualTypeArguments;
            }

            @Override
            public Type getRawType() {
                return rawType;
            }

            @Override
            public Type getOwnerType() {
                throw new UnsupportedOperationException();
            }
        };
    }

    private static List<Handler> createHandlers() {
        List<Handler> handlers = InjectorBuilder.createDefaultHandlers("ch.jalu.injector");
        handlers.removeIf(h -> h instanceof DefaultInjectionProvider);
        handlers.add(new StandardInjectionProvider());
        return handlers;
    }
}