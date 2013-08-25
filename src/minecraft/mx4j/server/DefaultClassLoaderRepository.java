/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.util.ArrayList;
import javax.management.loading.MLet;

/**
 * Default implementation of a ClassLoaderRepository
 *
 * @version $Revision: 1.8 $
 */
public class DefaultClassLoaderRepository extends ModifiableClassLoaderRepository
{
   private static final int WITHOUT = 1;
   private static final int BEFORE = 2;

   private ArrayList classLoaders = new ArrayList();

   public Class loadClass(String className) throws ClassNotFoundException
   {
      return loadClassWithout(null, className);
   }

   public Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException
   {
      return loadClassFromRepository(loader, className, WITHOUT);
   }

   public Class loadClassBefore(ClassLoader loader, String className) throws ClassNotFoundException
   {
      return loadClassFromRepository(loader, className, BEFORE);
   }

   protected void addClassLoader(ClassLoader cl)
   {
      if (cl == null) return;

      ArrayList loaders = getClassLoaders();
      synchronized (loaders)
      {
         if (!loaders.contains(cl)) loaders.add(cl);
      }
   }

   protected void removeClassLoader(ClassLoader cl)
   {
      if (cl == null) return;

      ArrayList loaders = getClassLoaders();
      synchronized (loaders)
      {
         loaders.remove(cl);
      }
   }

   protected ArrayList cloneClassLoaders()
   {
      ArrayList loaders = getClassLoaders();
      synchronized (loaders)
      {
         return (ArrayList)loaders.clone();
      }
   }

   protected ArrayList getClassLoaders()
   {
      return classLoaders;
   }

   private Class loadClassFromRepository(ClassLoader loader, String className, int algorithm) throws ClassNotFoundException
   {
      ArrayList copy = cloneClassLoaders();
      for (int i = 0; i < copy.size(); ++i)
      {
         try
         {
            ClassLoader cl = (ClassLoader)copy.get(i);
            if (cl.equals(loader))
            {
               if (algorithm == BEFORE)
                  break;
               else
                  continue;
            }

            return loadClass(cl, className);
         }
         catch (ClassNotFoundException ignored)
         {
         }
      }
      throw new ClassNotFoundException(className);
   }

   private Class loadClass(ClassLoader loader, String className) throws ClassNotFoundException
   {
      // This is an optimization: if the classloader is an MLet (and not a subclass)
      // then the method MLet.loadClass(String, ClassLoaderRepository) is used.
      if (loader.getClass() == MLet.class) return ((MLet)loader).loadClass(className, null);
      return loader.loadClass(className);
   }

   private int getSize()
   {
      ArrayList loaders = getClassLoaders();
      synchronized (loaders)
      {
         return loaders.size();
      }
   }
}
