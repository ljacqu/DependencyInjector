package ch.jalu.injector.handlers.postconstruct;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.PostConstruct;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * Searches newly instantiated classes for {@link PostConstruct} method
 * and validates their usage before executing it.
 */
public class PostConstructMethodInvoker implements PostConstructHandler {

    @Override
    public void process(Object object) {
        Class<?> clazz = object.getClass();
        while (clazz != null) {
            Method postConstructMethod = getAndValidatePostConstructMethod(clazz);
            if (postConstructMethod != null) {
                ReflectionUtils.invokeMethod(postConstructMethod, object);
            }
            clazz = clazz.getSuperclass();
        }
    }

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
