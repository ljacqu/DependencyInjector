package ch.jalu.injector.handlers.dependency.providers;

/**
 * Delta implementation.
 */
public class Delta2 implements Delta {

    private final String prefix;
    private Charlie charlie;

    public Delta2(String prefix) {
        this.prefix = prefix;
    }

    public void setCharlie(Charlie charlie) {
        this.charlie = charlie;
    }

    @Override
    public String getName() {
        return prefix + charlie.getString();
    }
}
