package com.github.sammyvimes.libproxpect;

import com.github.sammyvimes.aspect.cacheable.Cacheable;
import com.github.sammyvimes.aspect.timeout.Timeout;
import com.github.sammyvimes.aspect.timeout.TimeoutException;
import com.github.sammyvimes.libproxpect.proxy.AspectBinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

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
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class TimeoutAspectTestBase {

    private interface ISmallTimeout {

        String iAmFast();

        @Timeout(timeout = 1000)
        String dayumIAmSlow();
    }

    private class SmallTimeout implements ISmallTimeout {

        @Timeout(timeout = 2000)
        @Override
        public String iAmFast() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {}
            return "Meh";
        }

        @Override
        public String dayumIAmSlow() {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {}
            return "Meh";
        }

    }

    @Test
    public void testSimpleTimeout() throws Exception {
        AspectBinder.registerAspects(Timeout.class);
        ISmallTimeout proxified = AspectBinder.process(new SmallTimeout(), ISmallTimeout.class);
        Assert.assertEquals("Meh", proxified.iAmFast());
    }

    @Test(expected = TimeoutException.class)
    public void testTimeoutException() throws Exception {
        AspectBinder.registerAspects(Timeout.class);
        ISmallTimeout proxified = AspectBinder.process(new SmallTimeout(), ISmallTimeout.class);
        proxified.dayumIAmSlow();
    }

}
