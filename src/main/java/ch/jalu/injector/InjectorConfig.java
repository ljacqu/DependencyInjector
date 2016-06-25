package ch.jalu.injector;

import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Injector configurations.
 */
public class InjectorConfig {

    // Handlers
    private List<PreConstructHandler> preConstructHandlers = new ArrayList<>();
    private List<DependencyHandler> dependencyHandlers = new ArrayList<>();
    private List<PostConstructHandler> postConstructHandlers = new ArrayList<>();

    /**
     * Use the {@link InjectorBuilder} instead of instantiating this.
     */
    protected InjectorConfig() {
    }

    /**
     * Returns all registered {@link PreConstructHandler} instances.
     *
     * @return pre construct handlers
     */
    public List<PreConstructHandler> getPreConstructHandlers() {
        return preConstructHandlers;
    }

    /**
     * Returns all registered {@link DependencyHandler} instances.
     *
     * @return annotation handlers
     */
    public List<DependencyHandler> getDependencyHandlers() {
        return dependencyHandlers;
    }

    /**
     * Returns all registered {@link PostConstructHandler} instances.
     *
     * @return post construct handlers
     */
    public List<PostConstructHandler> getPostConstructHandlers() {
        return postConstructHandlers;
    }

    public void addPreConstructHandlers(List<? extends PreConstructHandler> preConstructHandlers) {
        InjectorUtils.checkNotNull(preConstructHandlers, null);
        this.preConstructHandlers.addAll(preConstructHandlers);
    }

    public void addAnnotationHandlers(List<? extends DependencyHandler> annotationHandlers) {
        InjectorUtils.checkNotNull(annotationHandlers, null);
        this.dependencyHandlers.addAll(annotationHandlers);
    }

    public void addPostConstructHandlers(List<? extends PostConstructHandler> postConstructHandlers) {
        InjectorUtils.checkNotNull(postConstructHandlers, null);
        this.postConstructHandlers.addAll(postConstructHandlers);
    }
}
