package ch.jalu.injector;

import java.lang.annotation.Annotation;

public interface Injector {

    void provide(Class<? extends Annotation> annotation, Object value);

    <T> T get(Class<T> clazz);

}