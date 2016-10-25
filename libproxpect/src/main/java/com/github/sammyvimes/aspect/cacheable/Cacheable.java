package com.github.sammyvimes.aspect.cacheable;

import com.github.sammyvimes.libproxpect.annotation.Aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Semyon on 26.02.2016.
 */
@Aspect(CacheableAspect.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Cacheable {

    //Time to cached value to live in millis
    //-1 to live forever
    long ttl() default -1;

}
