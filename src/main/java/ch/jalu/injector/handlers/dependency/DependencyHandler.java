package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ResolvedInstantiationContext;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.DependencyDescription;

import javax.annotation.Nullable;

/**
 * Handler which can provide dependencies in a custom manner based on the dependency's description.
 * <p>
 * For an example implementation, see {@link SavedAnnotationsHandler} which allows classes to declare
 * dependencies identified by custom annotations.
 */
public interface DependencyHandler extends Handler {

    /**
     * Resolves the value of a dependency based on the present annotations and the declared type.
     * Returns {@code null} if the given annotations and field type do not apply
     * to the handler. May throw an exception if a given annotation is being used wrong.
     * <p>
     * Note that you are you not forced to check if the returned Object is valid for the given
     * dependency {@code type}, unless you want to show a specific error message.
     *
     * @param context instantiation context
     * @param dependencyDescription description of the dependency
     * @return the resolved value, or null if not applicable
     * @throws Exception for invalid usage of annotation
     */
    @Nullable
    Object resolveValue(ResolvedInstantiationContext<?> context,
                        DependencyDescription dependencyDescription) throws Exception;

}
