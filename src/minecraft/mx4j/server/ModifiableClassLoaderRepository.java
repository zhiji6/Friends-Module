/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import javax.management.loading.ClassLoaderRepository;

/**
 * Base class to extend to create custom ClassLoaderRepositories.
 * MX4J's MBeanServer can use a custom ClassLoaderRepository instead of the default one
 * by simply specifying a suitable system property, see {@link mx4j.MX4JSystemKeys}.
 * It must be a class, otherwise it opens up a security hole, as anyone can cast the MBeanServer's
 * ClassLoaderRepository down to this class and call addClassLoader or removeClassLoader
 * since, if this class is an interface, they must be public.
 *
 * @version $Revision: 1.4 $
 */
public abstract class ModifiableClassLoaderRepository implements ClassLoaderRepository
{
   /**
    * Adds, if does not already exist, the specified ClassLoader to this repository.
    *
    * @param cl The classloader to add
    * @see #removeClassLoader
    */
   protected abstract void addClassLoader(ClassLoader cl);

   /**
    * Removes, if exists, the specified ClassLoader from this repository.
    *
    * @param cl The classloader to remove
    * @see #addClassLoader
    */
   protected abstract void removeClassLoader(ClassLoader cl);
}
