package ch.jalu.injector;

import ch.jalu.injector.annotationhandlers.AllInstancesAnnotationHandler;
import ch.jalu.injector.annotationhandlers.AllTypesAnnotationHandler;
import ch.jalu.injector.annotationhandlers.AnnotationHandler;
import ch.jalu.injector.annotationhandlers.AnnotationHandlerInjector;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Injector configurations.
 */
public class InjectorConfig {

    private String rootPackage = "";
    private List<AnnotationHandler> annotationHandlers;
    private AnnotationHandlerInjector annotationHandlerInjector;

    /**
     * Use the {@link InjectorBuilder} instead of instantiating this.
     */
    InjectorConfig() {
    }

    public List<AnnotationHandler> getAnnotationHandlers() {
        if (annotationHandlers == null) {
            // No annotation handlers yet set, so initialize the default
            annotationHandlers = new ArrayList<>();
            annotationHandlers.add(new AllTypesAnnotationHandler());
            annotationHandlers.add(new AllInstancesAnnotationHandler());
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

    public void injectAnnotationHandlerFields(Injector injector) {
        annotationHandlerInjector = new AnnotationHandlerInjector(rootPackage, injector);
        injectAnnotationHandlerFields(getAnnotationHandlers());
    }

    public void addAnnotationHandlers(List<? extends AnnotationHandler> annotationHandlers) {
        InjectorUtils.checkNotNull(annotationHandlers, null);
        injectAnnotationHandlerFields(annotationHandlers);
        getAnnotationHandlers().addAll(annotationHandlers);
    }

    public void setAnnotationHandlers(List<? extends AnnotationHandler> annotationHandlers) {
        this.annotationHandlers = new ArrayList<>(annotationHandlers);
    }

    private void injectAnnotationHandlerFields(List<? extends AnnotationHandler> handlers) {
        if (annotationHandlerInjector != null) {
            annotationHandlerInjector.inject(handlers);
        }
    }

    public boolean isAllowedPackage(String pkg) {
        return rootPackage == null || pkg.startsWith(rootPackage);
    }
}
