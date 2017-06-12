package ch.jalu.injector;

import ch.jalu.injector.handlers.annotationvalues.AnnotationValueHandler;
import ch.jalu.injector.handlers.dependency.DependencyHandler;
import ch.jalu.injector.handlers.instantiation.InstantiationProvider;
import ch.jalu.injector.handlers.postconstruct.PostConstructHandler;
import ch.jalu.injector.handlers.preconstruct.PreConstructHandler;
import ch.jalu.injector.handlers.provider.ProviderHandler;
import ch.jalu.injector.utils.InjectorUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Injector configuration.
 */
public class InjectorConfig {

    // Handlers
    private List<AnnotationValueHandler> annotationValueHandlers = new ArrayList<>();
    private List<ProviderHandler> providerHandlers = new ArrayList<>();
    private List<PreConstructHandler> preConstructHandlers = new ArrayList<>();
    private List<InstantiationProvider> instantiationProviders = new ArrayList<>();
    private List<DependencyHandler> dependencyHandlers = new ArrayList<>();
    private List<PostConstructHandler> postConstructHandlers = new ArrayList<>();

    /**
     * Use the {@link InjectorBuilder} instead of instantiating this.
     */
    protected InjectorConfig() {
    }

    /**
     * Returns all registered {@link AnnotationValueHandler} instances.
     *
     * @return pre construct handlers
     */
    public List<AnnotationValueHandler> getAnnotationValueHandlers() {
        return annotationValueHandlers;
    }

    /**
     * Returns all registered {@link ProviderHandler} instances.
     *
     * @return pre construct handlers
     */
    public List<ProviderHandler> getProviderHandlers() {
        return providerHandlers;
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
     * Returns all registered {@link InstantiationProvider} instances.
     *
     * @return instantiation providers
     */
    public List<InstantiationProvider> getInstantiationProviders() {
        return instantiationProviders;
    }

    /**
     * Returns all registered {@link DependencyHandler} instances.
     *
     * @return dependency handlers
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

    public void addAnnotationValueHandlers(List<? extends AnnotationValueHandler> annotationValueHandlers) {
        InjectorUtils.checkNotNull(annotationValueHandlers, null);
        this.annotationValueHandlers.addAll(annotationValueHandlers);
    }

    public void addProviderHandlers(List<? extends ProviderHandler> providerHandlers) {
        InjectorUtils.checkNotNull(providerHandlers, null);
        this.providerHandlers.addAll(providerHandlers);
    }

    public void addPreConstructHandlers(List<? extends PreConstructHandler> preConstructHandlers) {
        InjectorUtils.checkNotNull(preConstructHandlers, null);
        this.preConstructHandlers.addAll(preConstructHandlers);
    }

    public void addInstantiationProviders(List<? extends InstantiationProvider> instantiationProviders) {
        InjectorUtils.checkNotNull(instantiationProviders, null);
        this.instantiationProviders.addAll(instantiationProviders);
    }

    public void addDependencyHandlers(List<? extends DependencyHandler> dependencyHandlers) {
        InjectorUtils.checkNotNull(dependencyHandlers, null);
        this.dependencyHandlers.addAll(dependencyHandlers);
    }

    public void addPostConstructHandlers(List<? extends PostConstructHandler> postConstructHandlers) {
        InjectorUtils.checkNotNull(postConstructHandlers, null);
        this.postConstructHandlers.addAll(postConstructHandlers);
    }
}
