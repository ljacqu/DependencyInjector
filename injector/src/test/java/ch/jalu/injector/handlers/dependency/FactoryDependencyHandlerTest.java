package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.InjectorImpl;
import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.factory.Factory;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DefaultInjectionProvider;
import ch.jalu.injector.handlers.instantiation.Resolution;
import ch.jalu.injector.handlers.instantiation.StandardInjectionProvider;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.inheritance.Child;
import ch.jalu.injector.samples.inheritance.Grandparent;
import ch.jalu.injector.samples.inheritance.Parent;
import ch.jalu.injector.utils.InjectorUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Type;
import java.util.List;

import static ch.jalu.injector.TestUtils.createParameterizedType;
import static ch.jalu.injector.TestUtils.findOrThrow;
import static ch.jalu.injector.context.StandardResolutionType.SINGLETON;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link FactoryDependencyHandler}.
 */
@ExtendWith(MockitoExtension.class)
public class FactoryDependencyHandlerTest {

    private Injector injector;

    @BeforeEach
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

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> factory.newInstance((Class) Grandparent.class));

        // then
        assertThat(ex.getMessage(), containsString("not child of " + Parent.class));
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
        ResolutionContext context = new ResolutionContext(injector, newIdentifier(Factory.class));

        // when
        InjectorException ex = assertThrows(InjectorException.class, () -> getFactoryHandler().resolve(context));

        // then
        assertThat(ex.getMessage(), containsString("Factory fields must have concrete generic type."));
    }

    @Test
    public void shouldReturnNullForNonFactoryType() {
        // given
        ResolutionContext context = new ResolutionContext(injector, newIdentifier(Parent.class));

        // when
        Resolution<?> result = getFactoryHandler().resolve(context);

        // then
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unchecked")
    private <T> Factory<T> getFactoryForClass(Class<T> clazz) {
        FactoryDependencyHandler factoryHandler = getFactoryHandler();
        Resolution<?> instantiation = factoryHandler.resolve(new ResolutionContext(
            injector, newIdentifier(createParameterizedType(Factory.class, clazz))));
        InjectorUtils.checkArgument(instantiation.getDependencies().isEmpty(),
            "Expected to receive an instantiation method with no required dependencies");
        return (Factory<T>) instantiation.instantiateWith();
    }

    private FactoryDependencyHandler getFactoryHandler() {
        List<Handler> dependencyHandlers = ((InjectorImpl) injector).getConfig().getHandlers();
        Handler factoryHandler = findOrThrow(dependencyHandlers, handler -> handler instanceof FactoryDependencyHandler);
        return (FactoryDependencyHandler) factoryHandler;
    }

    private static List<Handler> createHandlers() {
        List<Handler> handlers = InjectorBuilder.createDefaultHandlers("ch.jalu.injector");
        handlers.removeIf(h -> h instanceof DefaultInjectionProvider);
        handlers.add(new StandardInjectionProvider());
        return handlers;
    }

    private static ObjectIdentifier newIdentifier(Type type) {
        return new ObjectIdentifier(SINGLETON, type);
    }
}