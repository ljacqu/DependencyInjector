package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.exceptions.InjectorException;

import javax.inject.Inject;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * The injector's default instantiation provider. Extending {@link StandardInjection}, it roughly follows the
 * documentation of {@link Inject} with additional restrictions:
 * <ul>
 *  <li>An exception is thrown if a class with an {@code @Inject} constructor also has fields with {@code @Inject}.
 *      In parent classes, {@code @Inject} fields are accepted.</li>
 *  <li>{@code @Inject} fields may not be static.</li>
 *  <li>This handler also checks that the class to instantiate is in the allowed root package.</li>
 * </ul>
 */
public class DefaultInjectionProvider extends StandardInjectionProvider {

    private final String rootPackage;

    public DefaultInjectionProvider(String rootPackage) {
        this.rootPackage = rootPackage;
    }

    @Override
    public <T> Resolution<T> safeGet(Class<T> clazz) {
        verifyIsClassPackageAllowed(clazz);
        return super.safeGet(clazz);
    }

    @Override
    protected void validateInjection(Class<?> clazz, Constructor<?> constructor, List<Field> fields) {
        super.validateInjection(clazz, constructor, fields);
        verifyIsClassPackageAllowed(clazz);

        final boolean hasConstructionInjection = constructor.isAnnotationPresent(Inject.class);
        for (Field field : fields) {
            if (hasConstructionInjection && field.getDeclaringClass() == clazz) {
                throw new InjectorException(clazz + " may not have @Inject constructor and @Inject fields. "
                    + "Pass the fields via the constructor as well, remove the @Inject constructor, or use "
                    + StandardInjectionProvider.class.getSimpleName() + " instead");
            }
            if (Modifier.isStatic(field.getModifiers())) {
                throw new InjectorException("@Inject may not be placed on static fields "
                    + "(found violation: '" + field + "')");
            }
        }
    }

    protected void verifyIsClassPackageAllowed(Class<?> clazz) {
        String packageName = clazz.getPackage().getName();
        if (!packageName.startsWith(rootPackage)) {
            throw new InjectorException("Class '" + clazz + "' with package '" + packageName + "' is outside of the "
                + "allowed packages. It must be provided explicitly or the package must be passed to the constructor.");
        }
    }
}
