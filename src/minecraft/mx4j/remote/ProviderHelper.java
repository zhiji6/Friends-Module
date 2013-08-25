/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.security.AccessController;
import java.security.PrivilegedAction;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.5 $
 */
public abstract class ProviderHelper
{
   protected static String normalizeProtocol(String protocol)
   {
      // Replace special chars as required by the spec
      String normalized = protocol.replace('+', '.');
      normalized = normalized.replace('-', '_');
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Normalizing protocol: " + protocol + " --> " + normalized);
      return normalized;
   }

   protected static String findSystemPackageList(final String key)
   {
      Logger logger = getLogger();
      String providerPackages = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty(key);
         }
      });
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Packages in the system property '" + key + "': " + providerPackages);
      return providerPackages;
   }

   protected static Class loadClass(String className, ClassLoader loader) throws ClassNotFoundException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Loading class: " + className + " with classloader " + loader);
      return loader.loadClass(className);
   }

   protected static String constructClassName(String packageName, String protocol, String className)
   {
      return new StringBuffer(packageName).append(".").append(protocol).append(".").append(className).toString();
   }

   protected static Logger getLogger()
   {
      return Log.getLogger(ProviderHelper.class.getName());
   }
}
