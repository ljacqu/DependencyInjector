package ch.jalu.injector;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.samples.subpackage.SubpackageClass;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * Test for {@link InjectorBuilder}.
 */
public class InjectorBuilderTest {

    /**
     * Tests that the allowed packages are set from the builder ot the injector,
     * and tests that the feature works as expected in {@link InjectorImpl}.
     */
    @Test
    public void shouldSupplyInjectorWithPackageSetting() {
        // create injector via builder
        Injector injector = new InjectorBuilder()
            .addDefaultHandlers(getClass().getPackage().getName() + ".samples.subpackage")
            .create();

        // register AlphaService and GammaService
        AlphaService alphaService = AlphaService.newInstance(new ProvidedClass(""));
        GammaService gammaService = new GammaService(alphaService);
        injector.register(AlphaService.class, alphaService);
        injector.register(GammaService.class, gammaService);

        // make sure we can instantiate SubpackageClass
        SubpackageClass subpackageClass = injector.getSingleton(SubpackageClass.class);
        assertThat(subpackageClass, not(nullValue()));

        // expect exception if we try to instantiate something outside of subpackage
        try {
            injector.getSingleton(BetaManager.class);
            fail("Expected exception");
        } catch (InjectorException ex) {
            assertThat(ex.getMessage(), containsString("outside of the allowed packages"));
        }
    }
}
