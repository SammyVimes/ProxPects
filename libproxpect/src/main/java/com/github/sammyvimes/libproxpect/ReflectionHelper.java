package com.github.sammyvimes.libproxpect;

import java.lang.reflect.Method;

/**
 * Created by Semyon on 27.02.2016.
 */
public class ReflectionHelper {

    public static Method getOverriddenMethod(final Method interfaceMethod, final Object impl) throws NoSuchMethodException {
        return impl.getClass().getDeclaredMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
    }

}
