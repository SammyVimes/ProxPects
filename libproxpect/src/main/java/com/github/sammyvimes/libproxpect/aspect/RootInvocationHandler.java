package com.github.sammyvimes.libproxpect.aspect;

import com.github.sammyvimes.libproxpect.proxy.AspectInvoker;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by Semyon on 26.02.2016.
 */
public class RootInvocationHandler implements AspectInvoker {

    private Map<MethodWrapper, ChainedInvocationHandler> handlersMap;

    public RootInvocationHandler(final Map<MethodWrapper, ChainedInvocationHandler> handlersMap) {
        this.handlersMap = handlersMap;
    }

    @Override
    public Object invoke(final Object receiver, final Object proxy, final Method method, final Object[] args) throws Throwable {
        ChainedInvocationHandler chainedInvocationHandler = handlersMap.get(new MethodWrapper(method));
        if (chainedInvocationHandler != null) {
            return chainedInvocationHandler.invoke(receiver, proxy, method, args);
        }
        return method.invoke(receiver, args);
    }


    /**
     * Класс нужный для хранения handler'a по ключу в Map
     * Одни и те же методы в наследнике и родителе -- разные объекты, поэтому
     * нужна {@link MethodWrapper#equals такая} проверка
     */
    public static class MethodWrapper {

        Method method;
        public MethodWrapper(final Method method) {
            this.method = method;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj != null && obj instanceof MethodWrapper) {
                MethodWrapper _other = (MethodWrapper) obj;
                Method other = _other.method;
                if ((method.getDeclaringClass() == other.getDeclaringClass())
                        && (method.getName().equals(other.getName()))) {
                    if (!method.getReturnType().equals(other.getReturnType())) {
                        return false;
                    }

                    Class<?>[] params1 = method.getParameterTypes();
                    Class<?>[] params2 = other.getParameterTypes();
                    if (params1.length == params2.length) {
                        for (int i = 0; i < params1.length; i++) {
                            // это классы, поэтому можно сравнивать их через ==
                            if (params1[i] != params2[i])
                                return false;
                        }
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public int hashCode() {
            return method != null ? method.getName().hashCode() : 0;
        }

    }

}
