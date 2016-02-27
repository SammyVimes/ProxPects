package com.github.sammyvimes.libproxpect.aspect;

import android.support.annotation.Nullable;

import com.github.sammyvimes.libproxpect.proxy.AspectInvoker;

import java.lang.reflect.Method;

/**
 * Created by Semyon on 26.02.2016.
 */
public abstract class ChainedInvocationHandler implements AspectInvoker {

    private ChainedInvocationHandler nestedHandler = null;

    public ChainedInvocationHandler() {
    }

    @Override
    public Object invoke(final Object receiver, final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object beforeValue = before(receiver, args);
        if (beforeValue != null) {
            return beforeValue;
        }
        Object interceptValue = intercept(receiver, args);
        if (interceptValue != null) {
            return interceptValue;
        }
        Object result = null;
        if (nestedHandler != null) {
            result = nestedHandler.invoke(receiver, proxy, method, args);
        } else {
            result = method.invoke(receiver, args);
        }
        Object afterValue = after(receiver, args, result);
        return afterValue != null ? afterValue : result;
    }

    public void setNestedHandler(final ChainedInvocationHandler nestedHandler) {
        this.nestedHandler = nestedHandler;
    }

    @Nullable
    protected abstract Object before(final Object receiver, final Object[] args);

    @Nullable
    protected abstract Object after(final Object receiver, final Object[] args, final Object result);

    @Nullable
    protected abstract Object intercept(final Object receiver, final Object[] args);

}
