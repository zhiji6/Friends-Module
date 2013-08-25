/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import javax.management.loading.ClassLoaderRepository;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * @version $Revision: 1.25 $
 */
public class MBeanServerFactory
{
   private static ArrayList servers = new ArrayList();

   private MBeanServerFactory()
   {
   }

   private static Logger getLogger()
   {
      return Log.getLogger(MBeanServerFactory.class.getName());
   }

   public static MBeanServer createMBeanServer()
   {
      return createMBeanServer(null);
   }

   public static MBeanServer createMBeanServer(String defaultDomain)
   {
      MBeanServer server = createMBeanServerImpl(defaultDomain, "createMBeanServer");
      synchronized (servers)
      {
         servers.add(server);
      }
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MBeanServer " + server + " registered successfully");
      return server;
   }

   public static MBeanServer newMBeanServer()
   {
      return newMBeanServer(null);
   }

   public static MBeanServer newMBeanServer(String defaultDomain)
   {
      return createMBeanServerImpl(defaultDomain, "newMBeanServer");
   }

   public static void releaseMBeanServer(MBeanServer server)
   {
      Logger logger = getLogger();
      try
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Releasing MBeanServer " + server);

         if (server != null)
         {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) sm.checkPermission(new MBeanServerPermission("releaseMBeanServer"));

            boolean removed = false;
            synchronized (servers)
            {
               removed = servers.remove(server);
            }

            if (removed)
            {
               if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MBeanServer " + server + " released successfully");
            }
            else
            {
               if (logger.isEnabledFor(Logger.INFO)) logger.info("MBeanServer " + server + " not released, cannot find it");
            }
         }
         else
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot release a null MBeanServer");
         }
      }
      catch (SecurityException x)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Security Exception caught while releasing MBeanServer " + server, x);
         throw x;
      }
   }

   public static ArrayList findMBeanServer(String id)
   {
      Logger logger = getLogger();
      ArrayList list = null;
      try
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Finding MBeanServer with ID: " + id);

         SecurityManager sm = System.getSecurityManager();
         if (sm != null) sm.checkPermission(new MBeanServerPermission("findMBeanServer"));

         if (id == null)
         {
            list = (ArrayList)servers.clone();
         }
         else
         {
            list = new ArrayList();
            synchronized (servers)
            {
               for (int i = 0; i < servers.size(); ++i)
               {
                  MBeanServer server = (MBeanServer)servers.get(i);
                  String serverId = getMBeanServerId(server);
                  if (id.equals(serverId))
                  {
                     list.add(server);
                     if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Found matching MBeanServer: " + server);
                  }
               }
            }
         }

         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("MBeanServer(s) found: " + list);

         return list;
      }
      catch (SecurityException x)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Security Exception caught while finding MBeanServer with ID: " + id, x);
         throw x;
      }
   }

   private static String getMBeanServerId(final MBeanServer server)
   {
      try
      {
         return (String)AccessController.doPrivileged(new PrivilegedExceptionAction()
         {
            public Object run() throws Exception
            {
               return server.getAttribute(ObjectName.getInstance("JMImplementation:type=MBeanServerDelegate"), "MBeanServerId");
            }
         });
      }
      catch (SecurityException x)
      {
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("No permission to get MBeanServerID", x);
      }
      catch (PrivilegedActionException x)
      {
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Could not get MBeanServerID", x.getException());
      }
      catch (Throwable x)
      {
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Could not get MBeanServerID", x);
      }
      return null;
   }

   public static ClassLoaderRepository getClassLoaderRepository(MBeanServer server)
   {
      // Yes, throw NPE is server is null (spec compliant)
      return server.getClassLoaderRepository();
   }

   private static MBeanServer createMBeanServerImpl(String domain, String permission)
   {
      Logger logger = getLogger();
      boolean trace = logger.isEnabledFor(Logger.TRACE);
      try
      {
         SecurityManager sm = System.getSecurityManager();
         if (sm != null) sm.checkPermission(new MBeanServerPermission(permission));

         // get MBeanServerBuilder

         if (trace) logger.trace("Obtaining MBeanServerBuilder");
         final MBeanServerBuilder builder = createMBeanServerBuilder();
         if (trace) logger.trace("Using MBeanServerBuilder " + builder.getClass());

         // create delegate

         if (trace) logger.trace("Creating MBeanServerDelegate...");
         final MBeanServerDelegate delegate = builder.newMBeanServerDelegate();
         if (trace) logger.trace("MBeanServerDelegate " + delegate.getClass() + " created successfully");

         // create MBean server

         if (trace) logger.trace("Creating MBeanServer...");
         MBeanServer mbs = builder.newMBeanServer(domain, null, delegate);
         if (trace) logger.trace("MBeanServer " + mbs + " created successfully");
         if (logger.isEnabledFor(Logger.INFO))
         {
            String mbeanServerId = getMBeanServerId(mbs);
            if (mbeanServerId != null)
               logger.info("Created MBeanServer with ID: " + mbeanServerId);
            else
               logger.info("Created MBeanServer");
         }
         return mbs;
      }
      catch (SecurityException x)
      {
         if (trace) logger.trace("Security Exception caught while creating an MBeanServer", x);
         throw x;
      }
   }


   private static MBeanServerBuilder createMBeanServerBuilder()
   {
      final Class builderClass = loadMBeanServerBuilderClass();
      try
      {
         return (MBeanServerBuilder)builderClass.newInstance();
      }
      catch (ClassCastException e)
      {
         throw new JMRuntimeException("Specified MBeanServerBuilder must be a subclass of MBeanServerBuilder: " + builderClass);
      }
      catch (IllegalAccessException e)
      {
         throw new JMRuntimeException("Can't instantiate MBeanServerBuilder " + builderClass + ": " + e);
      }
      catch (InstantiationException e)
      {
         throw new JMRuntimeException("Can't instantiate MBeanServerBuilder " + builderClass + ": " + e);
      }
   }


   private static Class loadMBeanServerBuilderClass()
   {
      String builderClassName = (String)AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            return System.getProperty("javax.management.builder.initial");
         }
      });

      if (builderClassName == null || builderClassName.length() == 0)
      {
         return MBeanServerBuilder.class;
      }

      try
      {
         return Thread.currentThread().getContextClassLoader().loadClass(builderClassName);
      }
      catch (ClassNotFoundException e)
      {
         throw new JMRuntimeException("MBeanServerBuilder class not found: " + builderClassName);
      }
   }
}
