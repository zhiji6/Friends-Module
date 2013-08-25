/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.loading;

/**
 * A repository for ClassLoader MBeans.
 * A ClassLoaderRepository contains the ClassLoader that loaded the MBeanServer and
 * registered MBeans that are ClassLoader subclasses and that does not implement the
 * {@link PrivateClassLoader} interface.
 * The order of registration for ClassLoader MBeans is important, as it will define
 * the behavior of MLets, see {@link MLet#findClass}; the MBeanServer's ClassLoader is
 * always the first ClassLoader.
 *
 * @version $Revision: 1.6 $
 * @see MLet
 * @see javax.management.MBeanServer#getClassLoaderRepository
 */
public interface ClassLoaderRepository
{
   /**
    * Loads the given class iterating through the list of classloaders contained in this repository,
    * from the first to the last.
    * The method returns as soon as the class is found, or throws a ClassNotFoundException
    *
    * @param className The name of the class to load
    * @return The loaded class
    * @throws ClassNotFoundException If the class is not found
    * @see #loadClassBefore
    */
   public Class loadClass(String className) throws ClassNotFoundException;

   /**
    * Loads the given class iterating through the list of classloaders contained in this repository,
    * from the first to the last, excluded the specified ClassLoader.
    * The method returns as soon as the class is found, or throws a ClassNotFoundException
    *
    * @param loader    The ClassLoader that should not be asked to load the class
    * @param className The name of the class to load
    * @return The loaded class
    * @throws ClassNotFoundException If the class is not found
    * @see #loadClassBefore
    */
   public Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException;

   /**
    * Loads the given class iterating through the list of classloaders contained in this repository,
    * from the first to the specified ClassLoader (that is not asked to load the class).
    *
    * @param loader    The ClassLoader that should not be asked to load the class and where the search must stop
    * @param className The name of the class to load
    * @return The loaded class
    * @throws ClassNotFoundException If the class is not found
    */
   public Class loadClassBefore(ClassLoader loader, String className) throws ClassNotFoundException;
}