package com.github.sammyvimes.libproxpect.annotation;

import com.github.sammyvimes.libproxpect.aspect.ChainedInvocationHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Semyon on 26.02.2016.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface Aspect {

    Class<? extends ChainedInvocationHandler> value();

}
