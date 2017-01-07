package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.Nullable;
import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Searches newly instantiated classes for {@link PostConstruct} method
 * and validates their usage before executing it.
 */
public class PostConstructMethodInvoker implements PostConstructHandler {

    @Override
    public <T> T process(T object) {
        Class<?> clazz = object.getClass();
        List<Method> postConstructMethods = getPostConstructMethods(clazz);
        for (int i = postConstructMethods.size() - 1; i >= 0; --i) {
            ReflectionUtils.invokeMethod(postConstructMethods.get(i), object);
        }
        return null;
    }

    private static List<Method> getPostConstructMethods(Class<?> clazz) {
        List<Method> postConstructMethods = new ArrayList<>();
        while (clazz != null) {
            Method postConstruct = getAndValidatePostConstructMethod(clazz);
            if (postConstruct != null) {
                postConstructMethods.add(postConstruct);
            }
            clazz = clazz.getSuperclass();
        }
        return postConstructMethods;
    }

    @Nullable
    private static Method getAndValidatePostConstructMethod(Class<?> clazz) {
        Method postConstructMethod = null;
        for (Method method : ReflectionUtils.safeGetDeclaredMethods(clazz)) {
            if (method.isAnnotationPresent(PostConstruct.class)) {
                if (postConstructMethod != null) {
                    throw new InjectorException("Multiple methods with @PostConstruct on " + clazz);
                } else if (method.getParameterTypes().length > 0 || Modifier.isStatic(method.getModifiers())) {
                    throw new InjectorException("@PostConstruct method may not be static or have any parameters. "
                            + "Invalid method in " + clazz);
                } else if (method.getReturnType() != void.class) {
                    throw new InjectorException("@PostConstruct method must have return type void. "
                            + "Offending class: " + clazz);
                } else {
                    postConstructMethod = method;
                }
            }
        }
        return postConstructMethod;
    }

}
