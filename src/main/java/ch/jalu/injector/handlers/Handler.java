package ch.jalu.injector.handlers;

/**
 * Handlers may modify the injection process at different moments.
 * They allow you to supply custom injection methods, custom validation.
 * <p>
 * The following subtypes are recognized.
 *
 * <ul>
 * <li>{@link ch.jalu.injector.handlers.preconstruct.PreConstructHandler}: Validates the request for instantiating a
 *     class and offers the possibility to override the given class with a subclass.</li>
 * <li>{@link ch.jalu.injector.handlers.instantiation.InstantiationProvider}: For the given class, returns an
 *     {@link ch.jalu.injector.handlers.instantiation.Instantiation} when possible with which the injector will
 *     create an object.</li>
 * <li>{@link ch.jalu.injector.handlers.dependency.DependencyHandler}: Offers a way to resolve a given dependency
 *     required to instantiate a class, e.g. to implement custom behavior for annotations.</li>
 * <li>{@link ch.jalu.injector.handlers.postconstruct.PostConstructHandler}: Perform an action on an object that
 *     has been created with the injector. You can support {@code @PostConstruct} methods this way or perform some
 *     form of validation.</li>
 * </ul>
 *
 * Handlers are executed in the order that they are given to {@link ch.jalu.injector.InjectorBuilder}, so more important
 * handlers should come first.
 */
public interface Handler {
}
