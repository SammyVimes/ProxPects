package com.github.sammyvimes.libproxpect.proxy;

import com.github.sammyvimes.libproxpect.aspect.RootInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by Semyon on 26.02.2016.
 */
public class SharedInvocationHandler implements InvocationHandler {

    private Object receiver;

    private RootInvocationHandler rootInvocationHandler;

    public SharedInvocationHandler(final Object receiver, final RootInvocationHandler rootInvocationHandler) {
        this.receiver = receiver;
        this.rootInvocationHandler = rootInvocationHandler;
    }

    @Override
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        return rootInvocationHandler.invoke(receiver, proxy, method, args);
    }

}
