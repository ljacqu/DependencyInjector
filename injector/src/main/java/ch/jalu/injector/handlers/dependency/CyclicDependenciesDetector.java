package ch.jalu.injector.handlers.dependency;

import ch.jalu.injector.context.ObjectIdentifier;
import ch.jalu.injector.context.ResolutionContext;
import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.handlers.instantiation.Resolution;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Detects cycles in the dependencies based on the context's parents.
 * This handler should come at the start of the chain so it can stop it with an appropriate error message.
 * If not stopped by this handler, cyclic dependencies will cause a StackOverflowException.
 */
public class CyclicDependenciesDetector implements Handler {

    @Override
    public Resolution<?> resolve(ResolutionContext context) {
        ObjectIdentifier duplicateIdentifier = findRepeatedIdentifier(context);
        if (duplicateIdentifier != null) {
            String traversalList = buildParentsList(context);
            throw new InjectorException("Found cyclic dependency' - already traversed '" + duplicateIdentifier
                + "' (full traversal list: " + traversalList + " -> " + context.getIdentifier() + ")");
        }
        return null;
    }

    @Nullable
    private static ObjectIdentifier findRepeatedIdentifier(ResolutionContext context) {
        Set<Type> types = new HashSet<>();
        types.add(context.getIdentifier().getType());
        for (ResolutionContext parent : context.getParents()) {
            if (!types.add(parent.getIdentifier().getType())) {
                return parent.getIdentifier();
            }
        }
        return null;
    }

    private static String buildParentsList(ResolutionContext context) {
        return context.getParents().stream()
            .map(ctx -> ctx.getIdentifier().getType().getTypeName())
            .collect(Collectors.joining(" -> "));
    }
}
