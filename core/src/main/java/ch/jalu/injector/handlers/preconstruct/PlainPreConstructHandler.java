package ch.jalu.injector.handlers.preconstruct;

/**
 * Convenience base implementation of {@link PreConstructHandler}
 * for handlers which do not remap the class' type.
 */
public abstract class PlainPreConstructHandler implements PreConstructHandler {

    @Override
    public <T> Class<? extends T> accept(Class<T> clazz) throws Exception {
        process(clazz);
        return null;
    }

    /**
     * Processes the class.
     *
     * @param clazz the class
     * @throws Exception for failed validation or preconditions
     */
    protected abstract void process(Class<?> clazz) throws Exception;
}
