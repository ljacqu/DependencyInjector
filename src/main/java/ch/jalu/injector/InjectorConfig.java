package ch.jalu.injector;

import ch.jalu.injector.annotationhandlers.AllInstancesAnnotationHandler;
import ch.jalu.injector.annotationhandlers.AllTypesHandler;
import ch.jalu.injector.annotationhandlers.AnnotationHandler;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Configures an injector.
 */
public class InjectorConfig {
    private String rootPackage = "";
    private List<AnnotationHandler> annotationHandlers;

    /**
     * Use the {@link InjectorBuilder} instead of instantiating this.
     */
    InjectorConfig() {
    }

    public List<AnnotationHandler> getAnnotationHandlers() {
        if (annotationHandlers == null) {
            annotationHandlers = new ArrayList<>();
            annotationHandlers.add(new AllTypesHandler(null));
            annotationHandlers.add(new AllInstancesAnnotationHandler(null, null));
        }
        return annotationHandlers;
    }

    @Deprecated // Subject to change in future versions
    public String getRootPackage() {
        return rootPackage;
    }

    @Deprecated // Subject to change in future versions
    public void setRootPackage(String rootPackage) {
        InjectorUtils.checkNotNull(rootPackage, null);
        this.rootPackage = rootPackage;
    }

    public void addAnnotationHandlers(List<? extends AnnotationHandler> annotationHandlers) {
        InjectorUtils.checkNotNull(annotationHandlers, null);
        getAnnotationHandlers().addAll(annotationHandlers);
    }

    public void setAnnotationHandlers(List<? extends AnnotationHandler> annotationHandlers) {
        this.annotationHandlers = new ArrayList<>(annotationHandlers);
    }

    public boolean isAllowedPackage(String pkg) {
        return rootPackage == null || pkg.startsWith(rootPackage);
    }
}
