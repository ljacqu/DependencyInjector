package ch.jalu.injector.handlers.instantiation;

import ch.jalu.injector.Injector;
import ch.jalu.injector.InjectorBuilder;
import ch.jalu.injector.handlers.Handler;
import ch.jalu.injector.samples.AlphaService;
import ch.jalu.injector.samples.BetaManager;
import ch.jalu.injector.samples.GammaService;
import ch.jalu.injector.samples.ProvidedClass;
import ch.jalu.injector.utils.ReflectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

/**
 * Test for {@link InstantiationCache}.
 */
public class InstantiationCacheTest {

    private Injector injector;
    private DefaultInjectionProvider defaultInjectionProvider;
    private InstantiationCache instantiationCache;

    @BeforeEach
    public void setUpInjector() {
        List<Handler> handlers = InjectorBuilder.createDefaultHandlers("ch.jalu.injector.samples");
        InstantiationCache instantiationCache = new InstantiationCache();
        handlers.add(0, instantiationCache);

        List<Handler> handlerSpies = new ArrayList<>(handlers.size());
        for (Handler handler : handlers) {
            Handler spy = spy(handler);
            if (spy instanceof DefaultInjectionProvider) {
                this.defaultInjectionProvider = (DefaultInjectionProvider) spy;
            } else if (spy instanceof InstantiationCache) {
                this.instantiationCache = (InstantiationCache) spy;
            }
            handlerSpies.add(spy);
        }
        this.injector = new InjectorBuilder().addHandlers(handlerSpies).create();
    }

    @Test
    public void shouldNotCacheInstantiation() {
        // given
        injector.register(ProvidedClass.class, new ProvidedClass(""));
        injector.getSingleton(GammaService.class);
        injector.getSingleton(AlphaService.class);

        // when / then
        assertThat(getCacheMap(), anEmptyMap());
    }

    @Test
    public void shouldCacheRequestScopedInstantiations() {
        // given
        injector.register(ProvidedClass.class, new ProvidedClass(""));
        injector.getSingleton(GammaService.class);
        Mockito.reset(defaultInjectionProvider);

        // when
        BetaManager manager1 = injector.newInstance(BetaManager.class);
        BetaManager manager2 = injector.newInstance(BetaManager.class);

        // then
        assertThat(manager1, not(sameInstance(manager2)));
        assertThat(getCacheMap(), aMapWithSize(1));
        assertThat(getCacheMap().get(BetaManager.class), not(nullValue()));
        verify(defaultInjectionProvider, times(1)).safeGet(any(Class.class));
    }

    private Map<Class, WeakReference<Resolution>> getCacheMap() {
        try {
            Field field = InstantiationCache.class.getDeclaredField("entries");
            return (Map) ReflectionUtils.getFieldValue(field, instantiationCache);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

}