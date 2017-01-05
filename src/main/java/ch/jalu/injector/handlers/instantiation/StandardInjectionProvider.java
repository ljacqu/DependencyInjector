package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;
import ch.jalu.injector.utils.ReflectionUtils;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;

/**
 * The injector's default provider of {@link Instantiation} objects for classes. It roughly follows the conditions
 * as given in {@link Inject}. The following is a summary of the instantiation's behavior and its deviations.
 * <p>
 * If available, the constructor with @{@link Inject} is used. Multiple {@code &#64;Inject} constructors results in
 * an exception being thrown. Otherwise, if there is only one constructor, of public visibility taking no arguments,
 * it is taken. For private classes (inner classes), a sole no-args constructor is also considered.
 * Finally, the no-args constructor of any visibility is taken if it belongs to a class that has {@code &#64;Inject}
 * fields. In this case, the no-args constructor is not required to be the only constructor of the class.
 * <p>
 * Fields with {@code &#64;Inject} are injected. They may be static (not recommended). If a final field is annotated
 * with {@code &#64;Inject}, an exception is thrown.
 * <p>
 * Method injection is not supported. Consequently, if a method with {@code &#64;Inject} is found on the class or
 * any of its parents, an exception is thrown.
 * <p>
 * {@link ch.jalu.injector.annotations.NoMethodScan} and {@link ch.jalu.injector.annotations.NoFieldScan} are respected.
 */
public class StandardInjectionProvider extends DirectInstantiationProvider {

    @Override
    public <T> Instantiation<T> safeGet(Class<T> clazz) {
        Constructor<T> constructor = getInjectionConstructor(clazz);
        if (constructor == null) {
            return null;
        }

        List<Field> fields = getFieldsToInject(clazz);
        validateInjection(clazz, constructor, fields);
        return new StandardInjection<>(constructor, fields);
    }

    // -------------
    // Constructors
    // -------------

    /**
     * Returns the constructor to be used for injection. Throws an exception if there
     * are multiple {@code &#64;Inject} constructors.
     *
     * @param clazz the class to process
     * @param <T> the class' type
     * @return the constructor, or {@code null} if there is no constructor suitable for injection
     */
    @Nullable
    protected <T> Constructor<T> getInjectionConstructor(Class<T> clazz) {
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length == 1 && isSuitableNoArgsConstructor(constructors[0])) {
            return (Constructor<T>) constructors[0];
        }

        Constructor<?> matchingConstructor = null;
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Inject.class)) {
                if (matchingConstructor != null) {
                    throw new InjectorException("Class '" + clazz + "' may not have multiple @Inject constructors");
                }
                matchingConstructor = constructor;
            }
        }

        if (matchingConstructor == null) {
            // Compatibility: If a class has at least one field with @Inject, take a non-public no-args constructor
            return (Constructor<T>) getNoArgsConstructorIfHasInjectField(clazz);
        }
        return (Constructor<T>) matchingConstructor;
    }

    private static boolean isSuitableNoArgsConstructor(Constructor<?> c) {
        if (c.getParameterTypes().length > 0) {
            return false;
        }
        return !Modifier.isPrivate(c.getModifiers()) || Modifier.isPrivate(c.getDeclaringClass().getModifiers());
    }

    @Nullable
    private static Constructor<?> getNoArgsConstructorIfHasInjectField(Class<?> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            for (Field field : ReflectionUtils.safeGetDeclaredFields(clazz)) {
                if (field.isAnnotationPresent(Inject.class)) {
                    return constructor;
                }
            }
        } catch (NoSuchMethodException e) {
            // noop
        }
        return null;
    }

    // ------------
    // Fields
    // ------------

    /**
     * Returns the fields in the class and its parents that should be injected.
     *
     * @param clazz the class to process
     * @return the fields to inject
     */
    protected List<Field> getFieldsToInject(Class<?> clazz) {
        List<Field> fields = new LinkedList<>();
        while (clazz != null) {
            for (Field f : ReflectionUtils.safeGetDeclaredFields(clazz)) {
                if (f.isAnnotationPresent(Inject.class)) {
                    fields.add(f);
                }
            }
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    // ------------
    // Validation
    // ------------

    /**
     * Assures that the class and the members relevant for the instantiation form a valid combination.
     *
     * @param clazz the class to instantiate
     * @param constructor the constructor to instantiate with
     * @param fields the fields to inject
     */
    protected void validateInjection(Class<?> clazz, Constructor<?> constructor, List<Field> fields) {
        validateHasNoFinalFields(fields);
        validateHasNoInjectMethods(clazz);
    }

    private void validateHasNoFinalFields(List<Field> fields) {
        for (Field field : fields) {
            if (Modifier.isFinal(field.getModifiers())) {
                throw new InjectorException("Field '" + field + "' may not be final and have @Inject");
            }
        }
    }

    private void validateHasNoInjectMethods(Class<?> clazz) {
        while (clazz != null) {
            for (Method m : ReflectionUtils.safeGetDeclaredMethods(clazz)) {
                if (m.isAnnotationPresent(Inject.class)) {
                    throw new InjectorException("@Inject on methods is not supported, but found it on '" + m
                        + "' while trying to instantiate '" + clazz + "'");
                }
            }
            clazz = clazz.getSuperclass();
        }
    }
}
