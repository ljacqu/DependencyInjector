package ch.jalu.injector.context;

/**
 * The context in which a class should be resolved.
 */
public enum ResolutionType {

    /** The singleton instance of the class is requested. */
    SINGLETON,

    /** A request-scoped instance is requested (i.e. a new instance). */
    REQUEST_SCOPED,

    /** An instance of this class is requested to instantiate another class. */
    DEPENDENCY

}
