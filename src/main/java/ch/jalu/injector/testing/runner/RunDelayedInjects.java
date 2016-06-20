package ch.jalu.injector.testing.runner;

import ch.jalu.injector.testing.BeforeInjecting;
import ch.jalu.injector.testing.InjectDelayed;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.runners.model.Statement;

import java.util.List;

/**
 * Statement for initializing {@link InjectDelayed} fields. These fields are
 * constructed after {@link BeforeInjecting} and before JUnit's &#064;Before.
 */
class RunDelayedInjects extends Statement {

    private final Statement next;
    private final Object target;
    private List<PendingInstantiation> pendingInstantiations;
    private InjectionResolver injectionResolver;

    public RunDelayedInjects(Statement next, List<PendingInstantiation> pendingInstantiations, Object target,
                             InjectionResolver injectionResolver) {
        this.next = next;
        this.pendingInstantiations = pendingInstantiations;
        this.target = target;
        this.injectionResolver = injectionResolver;
    }

    @Override
    public void evaluate() throws Throwable {
        for (PendingInstantiation pendingInstantiation : pendingInstantiations) {
            if (ReflectionUtils.getFieldValue(pendingInstantiation.getField(), target) != null) {
                throw new IllegalStateException("Field with @InjectDelayed must be null on startup");
            }
            Object object = injectionResolver.instantiate(pendingInstantiation.getInstantiation());
            ReflectionUtils.setField(pendingInstantiation.getField(), target, object);
        }
        this.pendingInstantiations = null;
        this.injectionResolver = null;
        next.evaluate();
    }
}
