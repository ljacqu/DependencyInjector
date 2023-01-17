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
 * Provider of {@link Resolution} objects that roughly follows the documentation as given in {@link Inject}.
 * The following is a summary of the instantiation's behavior and its deviations.
 * <p>
 * If available, the constructor with @{@link Inject} is used. Multiple {@code @Inject} constructors results in
 * an exception being thrown. Otherwise, if there is only one constructor, of public visibility taking no arguments,
 * it is taken. For private classes (inner classes), a sole no-args constructor is also considered.
 * Finally, the no-args constructor of any visibility is taken if it belongs to a class that has {@code @Inject}
 * fields. In this case, the no-args constructor is not required to be the only constructor of the class.
 * <p>
 * Fields with {@code @Inject} are injected. They may be static (not recommended). If a final field is annotated
 * with {@code @Inject}, an exception is thrown.
 * <p>
 * Method injection is not supported. Consequently, if a method with {@code @Inject} is found on the class or
 * any of its parents, an exception is thrown.
 * <p>
 * {@link ch.jalu.injector.annotations.NoMethodScan} and {@link ch.jalu.injector.annotations.NoFieldScan} are respected.
 */
public class StandardInjectionProvider extends DirectInstantiationProvider {

    @Override
    public <T> Resolution<T> safeGet(Class<T> clazz) {
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
     * are multiple {@code @Inject} constructors.
     *
     * @param clazz the class to process
     * @param <T> the class's type
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
            return getNoArgsConstructorIfHasInjectField(clazz);
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
    private static <T> Constructor<T> getNoArgsConstructorIfHasInjectField(Class<T> clazz) {
        try {
            Constructor<?> constructor = clazz.getDeclaredConstructor();
            for (Field field : ReflectionUtils.safeGetDeclaredFields(clazz)) {
                if (field.isAnnotationPresent(Inject.class)) {
                    return (Constructor<T>) constructor;
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
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            for (Field f : ReflectionUtils.safeGetDeclaredFields(currentClass)) {
                if (f.isAnnotationPresent(Inject.class)) {
                    fields.add(f);
                }
            }
            currentClass = currentClass.getSuperclass();
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
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            for (Method m : ReflectionUtils.safeGetDeclaredMethods(currentClass)) {
                if (m.isAnnotationPresent(Inject.class)) {
                    throw new InjectorException("@Inject on methods is not supported, but found it on '" + m
                        + "' while trying to instantiate '" + currentClass + "'");
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}
