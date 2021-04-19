package ch.jalu.injector;

import ch.jalu.injector.demo.MyApp;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * Test for {@link MyApp} to ensure it returns the expected result.
 */
class InjectorDemoTest {

    @Test
    void shouldRunDemo() {
        assertThat(MyApp.setUpAndRun(), equalTo("Result of the calculation: 754.7579"));
    }

}
