package com.github.sammyvimes.aspect.timeout;

import com.github.sammyvimes.aspect.cacheable.CacheableAspect;
import com.github.sammyvimes.libproxpect.annotation.Aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
@Aspect(TimeoutAspect.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timeout {

    /**
     * Method execution timeout, defaults to 1 second
     */
    long timeout() default 1000;

    /**
     * Maximum concurrent calls to the method
     */
    int maxParallel() default 1;

}
