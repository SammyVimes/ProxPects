package com.github.sammyvimes.libproxpect.annotation.base;

import com.github.sammyvimes.libproxpect.annotation.Aspect;
import com.github.sammyvimes.libproxpect.aspect.ChainedInvocationHandler;
import com.github.sammyvimes.libproxpect.aspect.base.CacheableAspect;

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

    long ttl() default -1;

}
