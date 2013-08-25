/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.loading;

import java.util.ArrayList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

/**
 * @version $Revision: 1.7 $
 * @deprecated No replacement. Just throw away all code that referenced this class, and use
 *             {@link javax.management.MBeanServer#getClassLoaderRepository} instead.
 */
public class DefaultLoaderRepository
{
   public DefaultLoaderRepository()
   {
   }

   public static Class loadClass(String className) throws ClassNotFoundException
   {
      return loadClassWithout(null, className);
   }

   public static Class loadClassWithout(ClassLoader loader, String className) throws ClassNotFoundException
   {
      ArrayList servers = MBeanServerFactory.findMBeanServer(null);
      for (int i = 0; i < servers.size(); ++i)
      {
         MBeanServer server = (MBeanServer)servers.get(i);
         ClassLoaderRepository repository = server.getClassLoaderRepository();
         try
         {
            return repository.loadClassWithout(loader, className);
         }
         catch (ClassNotFoundException ignored)
         {
         }
      }
      throw new ClassNotFoundException(className);
   }
}
