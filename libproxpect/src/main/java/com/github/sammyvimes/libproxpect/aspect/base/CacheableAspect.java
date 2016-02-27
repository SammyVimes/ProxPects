package com.github.sammyvimes.libproxpect.aspect.base;

import android.support.annotation.Nullable;

import com.github.sammyvimes.libproxpect.annotation.base.Cacheable;
import com.github.sammyvimes.libproxpect.aspect.ChainedInvocationHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Semyon on 26.02.2016.
 */
public class CacheableAspect extends ChainedInvocationHandler {

    private Cacheable cacheable;

    private long ttl;

    private Map<Object, Map<InvocationCacheKey, InvocationCacheValue>> objectMethodCallCache = new HashMap<>();

    public CacheableAspect(final Cacheable cacheable) {
        this.cacheable = cacheable;
        this.ttl = cacheable.ttl();
    }

    @Nullable
    @Override
    protected Object before(final Object receiver, final Object[] args) {
        Map<InvocationCacheKey, InvocationCacheValue> invocationCacheKeyInvocationCacheValueMap = objectMethodCallCache.get(receiver);
        if (invocationCacheKeyInvocationCacheValueMap == null) {
            return null;
        }
        InvocationCacheValue invocationCacheValue = invocationCacheKeyInvocationCacheValueMap.get(new InvocationCacheKey(args));

        if (invocationCacheValue != null) {
            long curTime = System.currentTimeMillis();
            if (curTime - invocationCacheValue.timestamp < ttl) {
                return invocationCacheValue.value;
            }
        }

        return null;
    }

    @Nullable
    @Override
    protected Object intercept(final Object receiver, final Object[] args) {
        return null;
    }

    @Nullable
    @Override
    protected Object after(final Object receiver, final Object[] args, final Object result) {
        Map<InvocationCacheKey, InvocationCacheValue> invocationCacheKeyInvocationCacheValueMap = objectMethodCallCache.get(receiver);
        if (invocationCacheKeyInvocationCacheValueMap == null) {
            invocationCacheKeyInvocationCacheValueMap = new HashMap<>();
            objectMethodCallCache.put(receiver, invocationCacheKeyInvocationCacheValueMap);
        }
        invocationCacheKeyInvocationCacheValueMap.put(new InvocationCacheKey(args), new InvocationCacheValue(System.currentTimeMillis(), result));
        return null;
    }

    private class InvocationCacheKey {

        Object[] args;

        public InvocationCacheKey(final Object[] args) {
            this.args = args;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            InvocationCacheKey that = (InvocationCacheKey) o;

            // Probably incorrect - comparing Object[] arrays with Arrays.equals
            return Arrays.equals(args, that.args);
        }

        @Override
        public int hashCode() {
            return args != null ? Arrays.hashCode(args) : 0;
        }

    }

    private class InvocationCacheValue {

        public InvocationCacheValue(final long timestamp, final Object value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        long timestamp;
        Object value;

    }

}
