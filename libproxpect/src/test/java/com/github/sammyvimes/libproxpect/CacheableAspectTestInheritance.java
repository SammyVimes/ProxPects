package com.github.sammyvimes.libproxpect;

import android.app.Activity;

import com.github.sammyvimes.aspect.cacheable.Cacheable;
import com.github.sammyvimes.libproxpect.proxy.AspectBinder;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/**
 * Created by Semyon on 27.02.2016.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class CacheableAspectTestInheritance {

    private interface Foo {

        String getValue();

        String getValue2();

    }

    private class FooImpl implements Foo {

        @Override
        @Cacheable(ttl = 100000)
        public String getValue() {
            return "just a value";
        }

        int count = 0;

        @Override
        @Cacheable(ttl = 100000)
        public String getValue2() {
            if (count == 0) {
                count++;
                return "this will be cached";
            }
            return "incorrect value";
        }

    }

    @Test
    public void testCacheSimple() throws Exception {
        AspectBinder.registerAspects(Cacheable.class);
        Foo proxified = AspectBinder.process(new FooImpl(), Foo.class);
        String value = proxified.getValue();
        Assert.assertEquals("just a value", value);
        value = proxified.getValue();
        Assert.assertEquals("just a value", value);
        String value2 = proxified.getValue2();
        Assert.assertEquals("this will be cached", value2);
        value2 = proxified.getValue2();
        Assert.assertEquals("this will be cached", value2);
    }

    private interface Foo2 {

        String getValue(final int i, final Activity activity);

        String getValue2(final int i, final Activity activity, final String strings);

    }

    private class FooImpl2 implements Foo2 {

        @Override
        @Cacheable(ttl = 100000)
        public String getValue(final int i, final Activity activity) {
            return "just a value";
        }

        int count = 0;

        @Override
        @Cacheable(ttl = 100000)
        public String getValue2(final int i, final Activity activity, final String strings) {
            if (count == 0) {
                count++;
                return "this will be cached";
            }
            return "incorrect value";
        }

    }

    @Test
    public void testMethodsWithEqualArguments() throws Exception {
        AspectBinder.registerAspects(Cacheable.class);
        Foo2 proxified = AspectBinder.process(new FooImpl2(), Foo2.class);
        String value = proxified.getValue(12, null);
        Assert.assertEquals("just a value", value);
        value = proxified.getValue(12, null);
        Assert.assertEquals("just a value", value);
        String value2 = proxified.getValue2(13, null, "test-string");
        Assert.assertEquals("this will be cached", value2);
        value2 = proxified.getValue2(13, null, "test-string");
        Assert.assertEquals("this will be cached", value2);
    }

    @Test
    public void testMethodsWithNotEqualArguments() throws Exception {
        AspectBinder.registerAspects(Cacheable.class);
        Foo2 proxified = AspectBinder.process(new FooImpl2(), Foo2.class);
        String value = proxified.getValue(12, null);
        Assert.assertEquals("just a value", value);
        value = proxified.getValue(13, null);
        Assert.assertEquals("just a value", value);
        String value2 = proxified.getValue2(13, null, "test-string");
        Assert.assertEquals("this will be cached", value2);
        value2 = proxified.getValue2(14, null, "test-string2");
        Assert.assertNotSame("this will be cached", value2);
    }

    private interface Foo3 {

        String getValue(final int i, final Activity activity);

        String getValue2(final int i, final Activity activity, final String strings);

        String notCacheableMethod();

    }

    private class FooImpl3 implements Foo3 {

        @Override
        @Cacheable(ttl = 100000)
        public String getValue(final int i, final Activity activity) {
            return "just a value";
        }

        int count = 0;

        @Override
        @Cacheable(ttl = 100000)
        public String getValue2(final int i, final Activity activity, final String strings) {
            if (count == 0) {
                count++;
                return "this will be cached";
            }
            return "incorrect value";
        }

        int count2 = 0;

        @Override
        public String notCacheableMethod() {
            if (count == 0) {
                count++;
                return "this can be cached";
            }
            return "correct value";
        }

    }

    @Test
    public void testNotCacheableMethod() throws Exception {
        AspectBinder.registerAspects(Cacheable.class);
        Foo3 proxified = AspectBinder.process(new FooImpl3(), Foo3.class);
        String value = proxified.notCacheableMethod();
        Assert.assertEquals("this can be cached", value);
        value = proxified.notCacheableMethod();
        Assert.assertEquals("correct value", value);
    }

}
