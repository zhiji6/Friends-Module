/*
Ziah_'s Client
Copyright (C) 2013  Ziah Jyothi

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see [http://www.gnu.org/licenses/].
*/

package com.oneofthesevenbillion.ziah.ZiahsClient;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This defines a Module.
 * Any class with this annotation will be loaded as a module.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Module {
    /**
     * A unique id for the Module
     */
    String moduleId();

    /**
     * A user friendly name for the Module
     */
    String name() default "";

    /**
     * A description for the Module
     */
    String description() default "";

    /**
     * A version string for this Module
     */
    String version() default "";

    /**
     * A simple comma separated list of Modules for this Module to be loaded after
     */
    String loadAfter() default "";

    /**
     * A simple comma separated list of required Modules
     */
    String requiredModules() default "";

    /**
     * A simple comma separated list of recommended Modules
     */
    String recommendedModules() default "";

    /**
     * A simple comma separated list of incompatible Modules
     */
    String incompatibleModules() default "";
}