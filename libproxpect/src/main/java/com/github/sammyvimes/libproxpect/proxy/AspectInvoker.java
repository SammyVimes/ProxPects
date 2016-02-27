package com.github.sammyvimes.libproxpect.proxy;

import java.lang.reflect.Method;

/**
 * Created by Semyon on 26.02.2016.
 */
public interface AspectInvoker {

    Object invoke(final Object receiver, final Object proxy, final Method method, final Object[] args) throws Throwable;

}
