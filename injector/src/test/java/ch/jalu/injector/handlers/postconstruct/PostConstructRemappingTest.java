package ch.jalu.injector.handlers.postconstruct;

//import ch.jalu.injector.InjectorBuilder;
//import ch.jalu.injector.InjectorImpl;
//import ch.jalu.injector.handlers.dependency.AllInstancesAnnotationHandler;
//import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
//import ch.jalu.injector.handlers.testimplementations.ProfilePostConstructHandler;
//import ch.jalu.injector.samples.animals.Sparrow;
//import ch.jalu.injector.samples.animals.services.Configuration;
//import ch.jalu.injector.samples.animals.services.HissService;
//import ch.jalu.injector.samples.animals.services.HissServiceProvider;
//import ch.jalu.injector.samples.animals.services.NameService;
//import ch.jalu.injector.samples.animals.services.SoundServiceSupervisor;
//import javassist.util.proxy.Proxy;
//import org.junit.Test;
//
//import static java.util.Collections.singletonList;
//import static org.hamcrest.Matchers.contains;
//import static org.hamcrest.Matchers.instanceOf;
//import static org.hamcrest.Matchers.not;
//import static org.junit.Assert.assertThat;

/**
 * Tests that {@link PostConstructHandler} implementations
 * can change the object that will be stored.
 */
public class PostConstructRemappingTest {

    private static final String PACKAGE = "ch.jalu.injector";

    // TODO #36: Fix test

//    @Test
//    public void shouldInitializeProfiledClassesWithProxy() {
//        // given
//        InjectorImpl injector = (InjectorImpl) new InjectorBuilder()
//            .addDefaultHandlers(PACKAGE)
//            .addHandlers(new AllInstancesAnnotationHandler(PACKAGE))
//            .create();
//        ProfilePostConstructHandler profileHandler = new ProfilePostConstructHandler(injector);
//        injector.getConfig().addPostConstructHandlers(singletonList(profileHandler));
//        injector.registerProvider(HissService.class, HissServiceProvider.class);
//
//        // when (trigger initialization + invoke some methods)
//        SoundServiceSupervisor supervisor = injector.getSingleton(SoundServiceSupervisor.class);
//        supervisor.muteAll();
//        injector.getSingleton(Sparrow.class).getName();
//
//        // then
//        assertThat(profileHandler.getInvocations(), contains("SoundServiceSupervisor#muteAll", "Configuration#getLang",
//            "NameService#constructName", "Configuration#getLang"));
//        assertThat(injector.getIfAvailable(Configuration.class), instanceOf(Proxy.class));
//        assertThat(injector.getIfAvailable(Sparrow.class), not(instanceOf(Proxy.class)));
//        assertThat(injector.getIfAvailable(NameService.class), instanceOf(Proxy.class));
//    }
}
