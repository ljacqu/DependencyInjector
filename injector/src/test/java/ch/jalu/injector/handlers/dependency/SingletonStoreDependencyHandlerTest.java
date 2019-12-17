package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.factory.SingletonStore;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.StandardInjectionProvider;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.inheritance.Child;
import ch.jalu.injector.samples.inheritance.ChildWithNoInjection;
import ch.jalu.injector.samples.inheritance.Grandparent;
import ch.jalu.injector.samples.inheritance.Parent;
import ch.jalu.injector.utils.InjectorUtils;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import static ch.jalu.injector.TestUtils.createParameterizedType;
import static ch.jalu.injector.TestUtils.findOrThrow;
import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link SingletonStoreDependencyHandler}.
 */
@ExtendWith(MockitoExtension.class)
class SingletonStoreDependencyHandlerTest {

    private Injector injector;

    @BeforeEach
    void setUpInjector() {
        injector = new InjectorBuilder().addHandlers(createHandlers()).create();
        injector.register(ProvidedClass.class, new ProvidedClass(""));

        // Trigger creation of the following singletons
        injector.getSingleton(Grandparent.class);
        injector.getSingleton(Parent.class);
        injector.getSingleton(Child.class);
        injector.getSingleton(ChildWithNoInjection.class);
    }

    @Test
    void shouldReturnAllGrandparentTypes() {
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
    void shouldThrowForClassNotWithinBounds() {
        // given
        SingletonStore<Parent> store = getSingletonStoreForClass(Parent.class);

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> store.retrieveAllOfType((Class) Grandparent.class));

        // then
        assertThat(ex.getMessage(), containsString("not child of " + Parent.class));
    }

    @Test
    void shouldThrowForSingletonCreationWithClassOutOfBounds() {
        // given
        SingletonStore<Child> store = getSingletonStoreForClass(Child.class);

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> store.getSingleton((Class) Parent.class));

        // then
        assertThat(ex.getMessage(), containsString("not child of " + Child.class));
    }

    @Test
    void shouldAllowObjectAsClass() {
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
    void shouldThrowForUnspecifiedGenerics() {
        // given
        ResolutionContext context = newContext(SingletonStore.class);

        // when
        InjectorException ex = assertThrows(InjectorException.class,
            () -> getSingletonStoreHandler().resolve(context));

        // then
        assertThat(ex.getMessage(), containsString("Singleton store fields must have concrete generic type."));
    }

    @Test
    void shouldReturnNullForOtherType() {
        // given
        ResolutionContext context = newContext(Parent.class);

        // when
        Resolution<?> result = getSingletonStoreHandler().resolve(context);

        // then
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unchecked")
    private <T> SingletonStore<T> getSingletonStoreForClass(Class<T> clazz) {
        SingletonStoreDependencyHandler singletonStoreHandler = getSingletonStoreHandler();
        Resolution<?> instantiation = singletonStoreHandler.resolve(
            newContext(createParameterizedType(SingletonStore.class, clazz)));
        InjectorUtils.checkArgument(instantiation.getDependencies().isEmpty(),
            "Expected to receive an instantiation method with no required dependencies");
        return (SingletonStore<T>) instantiation.instantiateWith();
    }

    private SingletonStoreDependencyHandler getSingletonStoreHandler() {
        List<Handler> dependencyHandlers = ((InjectorImpl) injector).getConfig().getHandlers();
        Handler singletonStoreHandler = findOrThrow(dependencyHandlers, handler -> handler instanceof SingletonStoreDependencyHandler);
        return (SingletonStoreDependencyHandler) singletonStoreHandler;
    }

    private static List<Handler> createHandlers() {
        List<Handler> handlers = InjectorBuilder.createDefaultHandlers("ch.jalu.injector");
        handlers.removeIf(h -> h instanceof DefaultInjectionProvider);
        handlers.add(new StandardInjectionProvider());
        return handlers;
    }

    private ResolutionContext newContext(Type clazz) {
        return new ResolutionContext(injector, new ObjectIdentifier(SINGLETON, clazz));
    }
}