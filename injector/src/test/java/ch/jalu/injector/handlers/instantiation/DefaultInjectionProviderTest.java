package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.ClassWithInjectMethod;
import ch.jalu.injector.samples.InjectOnDifferentMembersClass;
import ch.jalu.injector.samples.StaticFieldInjection;
import ch.jalu.injector.samples.inheritance.Child;
import ch.jalu.injector.samples.inheritance.ChildWithNoInjection;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test for {@link DefaultInjectionProvider}.
 */
class DefaultInjectionProviderTest {

    private DefaultInjectionProvider provider = new DefaultInjectionProvider("ch.jalu");

    @Test
    void shouldProvideInstantiation() {
        // given / when
        Resolution<BetaManager> injection = provider.safeGet(BetaManager.class);

        // then
        assertThat(injection, not(nullValue()));
        assertThat(injection.getDependencies(), hasSize(3));
    }

    @Test
    void shouldProvideInstantiationForClassWithInheritance() {
        // given / when
        Resolution<Child> injection = provider.safeGet(Child.class);

        // then
        assertThat(injection, not(nullValue()));
        assertThat(injection.getDependencies(), hasSize(5));
    }

    @Test
    void shouldThrowForStaticField() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.resolve(contextOf(StaticFieldInjection.class)));

        // then
        assertThat(ex.getMessage(), containsString("@Inject may not be placed on static fields"));
    }

    @Test
    void shouldThrowForMixedInjection() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.resolve(contextOf(InjectOnDifferentMembersClass.class)));

        // then
        assertThat(ex.getMessage(), containsString("may not have @Inject constructor and @Inject fields"));
    }

    @Test
    void shouldInjectClass() {
        // given / when
        Resolution<ChildWithNoInjection> injection = provider.safeGet(ChildWithNoInjection.class);

        // then
        assertThat(injection, not(nullValue()));
        assertThat(injection.getDependencies(), hasSize(2));
    }

    @Test
    void shouldThrowForInjectMethodInParent() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.safeGet(ChildOfParentWithInjectMethod.class));

        // then
        assertThat(ex.getMessage(), containsString("@Inject on methods is not supported"));
    }

    @Test
    void shouldRejectClassWithInvalidPackage() {
        // given / when
        InjectorException ex = assertThrows(InjectorException.class, () -> provider.resolve(contextOf(Object.class)));

        // then
        assertThat(ex.getMessage(), containsString("outside of the allowed packages"));
    }

    private static ResolutionContext contextOf(Class<?> clazz) {
        return new ResolutionContext(null, new ObjectIdentifier(null, clazz));
    }

    private static final class ChildOfParentWithInjectMethod extends ClassWithInjectMethod {

        @Inject
        private ChildOfParentWithInjectMethod(AlphaService alphaService) {

        }
    }

}