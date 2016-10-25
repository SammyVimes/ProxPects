package com.github.sammyvimes.libproxpect.aspect;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.github.sammyvimes.libproxpect.proxy.AspectInvoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Semyon on 26.02.2016.
 */
public abstract class ChainedInvocationHandler implements AspectInvoker {

    protected static final Object NOTHING = new Object();

    protected static final Object STOP = new Object();

    private ChainedInvocationHandler nestedHandler = null;

    public ChainedInvocationHandler() {
    }

    @Override
    public Object invoke(final Object receiver, final Object proxy, final Method method, final Object[] args) throws Throwable {
        Object beforeValue = before(receiver, method, args);
        if (beforeValue == STOP) {
            return null;
        }
        if (beforeValue != NOTHING) {
            return beforeValue;
        }
        Object interceptValue = intercept(receiver, method, args);
        if (interceptValue == STOP) {
            return null;
        }
        if (interceptValue != NOTHING) {
            return interceptValue;
        }
        Object result = null;
        if (nestedHandler != null) {
            result = nestedHandler.invoke(receiver, proxy, method, args);
        } else {
            result = method.invoke(receiver, args);
        }
        Object afterValue = after(receiver, method, args, result);
        if (afterValue == STOP) {
            return null;
        }
        return afterValue != NOTHING ? afterValue : result;
    }

    public void setNestedHandler(final ChainedInvocationHandler nestedHandler) {
        this.nestedHandler = nestedHandler;
    }

    @NonNull
    protected abstract Object before(final Object receiver, final Method method, final Object[] args);

    @NonNull
    protected abstract Object after(final Object receiver, final Method method, final Object[] args, final Object result);

    @NonNull
    protected abstract Object intercept(final Object receiver, final Method method, final Object[] args) throws InvocationTargetException, IllegalAccessException;

    @NonNull
    public ChainedInvocationHandler getBottomHandler() {
        ChainedInvocationHandler bottom = this;
        while (bottom.nestedHandler != null) {
            bottom = bottom.nestedHandler;
        }
        return bottom;
    }

}
