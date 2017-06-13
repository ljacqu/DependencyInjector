package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.TestUtils.ExceptionCatcher;
import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.context.StandardResolutionType;
import ch.jalu.injector.factory.SingletonStore;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;
import ch.jalu.injector.handlers.instantiation.StandardInjectionProvider;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.inheritance.Child;
import ch.jalu.injector.samples.inheritance.ChildWithNoInjection;
import ch.jalu.injector.samples.inheritance.Grandparent;
import ch.jalu.injector.samples.inheritance.Parent;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static ch.jalu.injector.TestUtils.findOrThrow;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;

/**
 * Test for {@link SingletonStoreDependencyHandler}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SingletonStoreDependencyHandlerTest {

    private Injector injector;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    private ExceptionCatcher exceptionCatcher = new ExceptionCatcher(expectedException);

    @Before
    public void setUpInjector() {
        injector = new InjectorBuilder().addHandlers(createHandlers()).create();
        injector.register(ProvidedClass.class, new ProvidedClass(""));

        // Trigger creation of the following singletons
        injector.getSingleton(Grandparent.class);
        injector.getSingleton(Parent.class);
        injector.getSingleton(Child.class);
        injector.getSingleton(ChildWithNoInjection.class);
    }

    @Test
    public void shouldReturnAllGrandparentTypes() {
        // given
        SingletonStore<Grandparent> store = getSingletonStoreForClass(Grandparent.class);

        // when
        Grandparent grandparent = store.getSingleton(Grandparent.class);
        Collection<Grandparent> allSingletons = store.retrieveAllOfType();
        Collection<Parent> allParents = store.retrieveAllOfType(Parent.class);
        Collection<Child> allChildren = store.retrieveAllOfType(Child.class);

        // then
        final Parent parent = injector.getSingleton(Parent.class);
        final ChildWithNoInjection child0 = injector.getSingleton(ChildWithNoInjection.class);
        final Child child = injector.getSingleton(Child.class);

        assertThat(grandparent, sameInstance(injector.getSingleton(Grandparent.class)));
        assertThat(allSingletons, Matchers.containsInAnyOrder(grandparent, parent, child, child0));
        assertThat(allParents, Matchers.containsInAnyOrder(parent, child));
        assertThat(allChildren, contains(child));
    }

    @Test
    public void shouldThrowForClassNotWithinBounds() {
        // given
        SingletonStore<Parent> store = getSingletonStoreForClass(Parent.class);

        // expect
        exceptionCatcher.expect("not child of " + Parent.class);

        // when
        store.retrieveAllOfType((Class) Grandparent.class);
    }

    @Test
    public void shouldThrowForSingletonCreationWithClassOutOfBounds() {
        // given
        SingletonStore<Child> store = getSingletonStoreForClass(Child.class);

        // expect
        exceptionCatcher.expect("not child of " + Child.class);

        // when
        store.getSingleton((Class) Parent.class);
    }

    @Test
    public void shouldAllowObjectAsClass() {
        // given
        SingletonStore<Object> store = getSingletonStoreForClass(Object.class);

        // when
        Collection<Object> allSingletons = store.retrieveAllOfType();
        Collection<BetaManager> betaManagers = store.retrieveAllOfType(BetaManager.class);
        Collection<Number> allNumbers = store.retrieveAllOfType(Number.class);

        // then
        // injector, alpha/beta/gamma service, providedClass, grandParent, parent, child, childWithNoInjection = 9
        assertThat(allSingletons, hasSize(9));
        assertThat(allSingletons, hasItems(injector.getSingleton(Child.class), injector.getSingleton(Grandparent.class),
            injector.getSingleton(BetaManager.class)));
        assertThat(betaManagers, contains(injector.getSingleton(BetaManager.class)));
        assertThat(allNumbers, empty());
    }

    @Test
    public void shouldThrowForUnspecifiedGenerics() {
        // given
        ResolvedInstantiationContext<BetaManager> context = new ResolvedInstantiationContext<>(
            injector, StandardResolutionType.SINGLETON, BetaManager.class, BetaManager.class, null);
        DependencyDescription description = new DependencyDescription(SingletonStore.class,  null);

        // expect
        exceptionCatcher.expect("Singleton store fields must have concrete generic type.");

        // when
        getSingletonStoreHandler().resolveValue(context, description);
    }

    @Test
    public void shouldReturnNullForOtherType() {
        // given
        ResolvedInstantiationContext<Object> context = new ResolvedInstantiationContext<>(
            injector, StandardResolutionType.SINGLETON, Object.class, Object.class, null);
        DependencyDescription description = new DependencyDescription(Parent.class, null);

        // when
        Object result = getSingletonStoreHandler().resolveValue(context, description);

        // then
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unchecked")
    private <T> SingletonStore<T> getSingletonStoreForClass(Class<T> clazz) {
        SingletonStoreDependencyHandler singletonStoreHandler = getSingletonStoreHandler();
        return (SingletonStore<T>) singletonStoreHandler.resolveValue(
            new ResolvedInstantiationContext<>(injector, StandardResolutionType.SINGLETON,
                Object.class, Object.class, null),
            new DependencyDescription(createParameterizedType(SingletonStore.class, clazz), null)
        );
    }

    private SingletonStoreDependencyHandler getSingletonStoreHandler() {
        List<Handler> dependencyHandlers = ((InjectorImpl) injector).getConfig().getHandlers();
        Handler singletonStoreHandler = findOrThrow(dependencyHandlers, handler -> handler instanceof SingletonStoreDependencyHandler);
        return (SingletonStoreDependencyHandler) singletonStoreHandler;
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