package ch.jalu.injector.extras.handlers;


import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * Remaps the class to instantiate to a concrete implementation for interfaces and abstract classes.
 * Does not do any remapping if no implementation or multiple implementations were found.
 */
public class AutowiringHandler implements PreConstructHandler {

    private Reflections reflections;
    private boolean throwIfNoUniqueSubtypeFound;

    /**
     * Constructor.
     *
     * @param rootPackage the package to scan for implementations
     */
    public AutowiringHandler(String rootPackage) {
        reflections = new Reflections(rootPackage);
    }

    @Override
    public <T> Class<? extends T> accept(Class<T> clazz) throws Exception {
        if (!isInstantiable(clazz)) {
            Set<Class<? extends T>> subtypes = reflections.getSubTypesOf(clazz);
            Class<? extends T> matchedSubtype = null;
            for (Class<? extends T> subtype : subtypes) {
                if (isInstantiable(subtype)) {
                    if (matchedSubtype == null) {
                        matchedSubtype = subtype;
                    } else {
                        if (throwIfNoUniqueSubtypeFound) {
                            throw new InjectorException("Found multiple subtypes for '" + clazz + "'");
                        }
                        return null;
                    }
                }
            }
            if (throwIfNoUniqueSubtypeFound && matchedSubtype == null) {
                throw new InjectorException("Did not find any subtype for '" + clazz + "'");
            }
            return matchedSubtype;
        }
        return null;
    }

    /**
     * Sets whether an exception should be thrown when no unique implementation could be found.
     *
     * @param throwIfNoUniqueSubtypeFound true to throw an exception, false to not do anything (default)
     */
    public void setThrowIfNoUniqueSubtypeFound(boolean throwIfNoUniqueSubtypeFound) {
        this.throwIfNoUniqueSubtypeFound = throwIfNoUniqueSubtypeFound;
    }

    private static boolean isInstantiable(Class<?> clazz) {
        return !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
    }
}
