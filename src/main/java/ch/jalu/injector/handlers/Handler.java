package ch.jalu.injector.handlers;

/**
 * Handlers may modify the injection process at different moments.
 * They allow you to supply custom injection methods, custom validation.
 * See the implementing interfaces for more information.
 *
 * @see ch.jalu.injector.handlers.preconstruct.PreConstructHandler
 * @see ch.jalu.injector.handlers.dependency.DependencyHandler
 * @see ch.jalu.injector.handlers.postconstruct.PostConstructHandler
 */
public interface Handler {
}
