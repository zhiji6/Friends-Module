/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.loading;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.io.StreamCorruptedException;
import java.lang.reflect.Proxy;

/**
 * ObjectInputStream that can read serialized java Objects using a supplied classloader
 * to find the object's classes.
 *
 * @version $Revision: 1.6 $
 */
public class ClassLoaderObjectInputStream extends ObjectInputStream
{
   private ClassLoader classLoader;

   /**
    * Creates a new ClassLoaderObjectInputStream
    *
    * @param stream      The decorated stream
    * @param classLoader The ClassLoader used to load classes
    */
   public ClassLoaderObjectInputStream(InputStream stream, ClassLoader classLoader) throws IOException, StreamCorruptedException
   {
      super(stream);
      this.classLoader = classLoader;
   }

   protected Class resolveClass(ObjectStreamClass osc) throws IOException, ClassNotFoundException
   {
      String name = osc.getName();
      return loadClass(name);
   }

   protected Class resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException
   {
      Class[] classes = new Class[interfaces.length];
      for (int i = 0; i < interfaces.length; ++i) classes[i] = loadClass(interfaces[i]);

      return Proxy.getProxyClass(classLoader, classes);
   }

   private Class loadClass(String name) throws ClassNotFoundException
   {
      if (classLoader != null)
         return classLoader.loadClass(name);
      else
         return Class.forName(name, true, null);
   }
}
