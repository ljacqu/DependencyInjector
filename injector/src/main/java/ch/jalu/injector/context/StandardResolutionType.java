package ch.jalu.injector.context;

/**
 * The context (scope) in which a class should be resolved.
 */
public enum StandardResolutionType implements ResolutionType {

    /** The singleton instance of the class is requested. */
    SINGLETON,

    /** A request-scoped instance is requested (i.e. a new instance). */
    REQUEST_SCOPED,

    /** Request-scoped, if all dependencies already exist. */
    REQUEST_SCOPED_IF_HAS_DEPENDENCIES

}
