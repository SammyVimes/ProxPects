package com.github.sammyvimes.aspect.timeout;

import android.support.annotation.NonNull;

import com.github.sammyvimes.libproxpect.aspect.ChainedInvocationHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * This file is part of ProxPects.
 * <p/>
 * ProxPects is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p/>
 * ProxPects is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with ProxPects.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * Created by Semyon on 25.10.2016.
 */
public class TimeoutAspect extends ChainedInvocationHandler {

    private Timeout timeoutAnnotation;

    private long timeout;

    private int maxParallel;

    private Executor executor = null;

    public TimeoutAspect(final Timeout timeoutAnnotation) {
        this.timeoutAnnotation = timeoutAnnotation;
        this.timeout = timeoutAnnotation.timeout();
        this.maxParallel = timeoutAnnotation.maxParallel();
        executor = Executors.newFixedThreadPool(this.maxParallel);
    }

    @NonNull
    @Override
    protected Object intercept(final Object receiver, final Method method, final Object[] args) throws InvocationTargetException, IllegalAccessException {
        final Object[] res = new Object[3];
        res[1] = false;
        res[2] = null;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Object callResult = null;
                Exception ex = null;
                try {
                    callResult = method.invoke(receiver, args);
                } catch (IllegalAccessException e) {
                    ex = e;
                } catch (InvocationTargetException e) {
                    ex = e;
                } catch (RuntimeException e) {
                    ex = e;
                }
                synchronized (res) {
                    res[0] = callResult;
                    res[1] = true;
                    res[2] = ex;
                    res.notify();
                }
            }
        });

        synchronized (res) {
            long timeToWait = timeout;
            while (timeToWait > 0) {
                boolean waitMore = (!(Boolean) res[1]) && res[2] == null;
                if (!waitMore) {
                    break;
                }

                long timestamp = System.currentTimeMillis();
                try {
                    res.wait(timeToWait);
                } catch (InterruptedException e) {
                } //somebody wakes us up, doesn't really matter
                long now = System.currentTimeMillis();
                long passedTime = now - timestamp;
                timeToWait -= passedTime;
            }

            Boolean callEnded = (Boolean) res[1];
            if (callEnded) {
                Object result = res[0];
                Exception ex = (Exception) res[2];
                if (ex != null) {
                    throw new RuntimeException(ex);
                }
                return result;
            } else {
                throw new TimeoutException("Method [" + method.getName() + "] of object [" + receiver + "] failed to execute in time");
            }
        }
    }


    @NonNull
    @Override
    protected Object before(final Object receiver, final Method method, final Object[] args) {
        return NOTHING;
    }

    @NonNull
    @Override
    protected Object after(final Object receiver, final Method method, final Object[] args, final Object result) {
        return NOTHING;
    }
}
