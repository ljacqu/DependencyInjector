package ch.jalu.injector;

import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class SampleTest {

    @Test
    public void demo() {
        String hello = "hello";

        assertThat(hello.toUpperCase(), equalTo("HELLO"));
    }
}
