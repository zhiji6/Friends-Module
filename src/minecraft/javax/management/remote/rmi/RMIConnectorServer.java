/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.remote.ConnectionResolver;
import mx4j.remote.MX4JRemoteUtils;

/**
 * @version $Revision: 1.16 $
 */
public class RMIConnectorServer extends JMXConnectorServer
{
   public static final String JNDI_REBIND_ATTRIBUTE = "jmx.remote.jndi.rebind";
   public static final String RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.client.socket.factory";
   public static final String RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE = "jmx.remote.rmi.server.socket.factory";

   private JMXServiceURL url;
   private final Map environment;
   private RMIServerImpl rmiServer;
   private final ClassLoader defaultClassLoader;
   private boolean active;
   private boolean stopped;

   public RMIConnectorServer(JMXServiceURL url, Map environment) throws IOException
   {
      this(url, environment, null, null);
   }

   public RMIConnectorServer(JMXServiceURL url, Map environment, MBeanServer server) throws IOException
   {
      this(url, environment, null, server);
   }

   public RMIConnectorServer(JMXServiceURL url, Map environment, RMIServerImpl rmiServer, MBeanServer server) throws IOException
   {
      super(server);
      if (url == null) throw new IllegalArgumentException("JMXServiceURL cannot be null");
      this.url = url;
      this.environment = environment == null ? new HashMap() : new HashMap(environment);
      this.rmiServer = rmiServer;
      this.defaultClassLoader = findDefaultClassLoader(this.environment, server);
   }

   private ClassLoader findDefaultClassLoader(Map environment, MBeanServer server) throws IllegalArgumentException
   {
      Object loader = environment.get(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER);
      Object loaderName = environment.get(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME);
      if (loader != null && loaderName != null) throw new IllegalArgumentException("Environment properties " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER + " and " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME + " cannot be both defined");

      if (loader != null)
      {
         if (!(loader instanceof ClassLoader))
            throw new IllegalArgumentException("Environment property " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER + " must be a ClassLoader");
         else
            return (ClassLoader)loader;
      }

      if (loaderName != null)
      {
         if (!(loaderName instanceof ObjectName)) throw new IllegalArgumentException("Environment property " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME + " must be an ObjectName");
         ObjectName name = (ObjectName)loaderName;
         try
         {
            if (!server.isInstanceOf(name, ClassLoader.class.getName())) throw new InstanceNotFoundException();
            return server.getClassLoader((ObjectName)loader);
         }
         catch (InstanceNotFoundException x)
         {
            throw new IllegalArgumentException("ObjectName " + name + " defined by environment property " + JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME + " must name a ClassLoader");
         }
      }

      return Thread.currentThread().getContextClassLoader();
   }

   public JMXServiceURL getAddress()
   {
      return url;
   }

   public Map getAttributes()
   {
      Map env = MX4JRemoteUtils.removeNonSerializableEntries(environment);
      return Collections.unmodifiableMap(env);
   }

   public boolean isActive()
   {
      return active;
   }

   private boolean isStopped()
   {
      return stopped;
   }

   public synchronized void start() throws IOException
   {
      Logger logger = getLogger();

      if (isActive())
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("This RMIConnectorServer has already been started");
         return;
      }
      if (isStopped())
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("This RMIConnectorServer has already been stopped");
         throw new IOException("This RMIConnectorServer has already been stopped");
      }

      MBeanServer server = getMBeanServer();
      if (server == null) throw new IllegalStateException("This RMIConnectorServer is not attached to an MBeanServer");

      JMXServiceURL address = getAddress();
      String protocol = address.getProtocol();
      ConnectionResolver resolver = ConnectionResolver.newConnectionResolver(protocol, environment);
      if (rmiServer == null)
      {
         if (resolver == null) throw new MalformedURLException("Unsupported protocol: " + protocol);
         rmiServer = (RMIServerImpl)resolver.createServer(address, environment);
      }

      rmiServer.setRMIConnectorServer(this);
      rmiServer.setMBeanServer(server);
      rmiServer.setDefaultClassLoader(defaultClassLoader);

      rmiServer.export();

      // Replace the JMXServiceURL, as it can now contain the encoded stub/ior
      url = resolver.bindServer(rmiServer, address, environment);

      active = true;

      if (logger.isEnabledFor(Logger.INFO)) logger.info("RMIConnectorServer started at: " + url);
   }

   public synchronized void stop() throws IOException
   {
      if (isStopped()) return;

      stopped = true;
      active = false;

      if (rmiServer != null) rmiServer.close();

      JMXServiceURL address = getAddress();
      String protocol = address.getProtocol();
      ConnectionResolver resolver = ConnectionResolver.newConnectionResolver(protocol, environment);
      if (resolver == null) throw new MalformedURLException("Unsupported protocol: " + protocol);
      resolver.unbindServer(rmiServer, address, environment);
      resolver.destroyServer(rmiServer, address, environment);

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.INFO)) logger.info("RMIConnectorServer stopped at: " + address);
   }

   public JMXConnector toJMXConnector(Map env) throws IOException
   {
      if (!isActive()) throw new IllegalStateException("This JMXConnectorServer has not been started");
      return super.toJMXConnector(env);
   }

   protected void connectionOpened(String connectionId, String message, Object userData)
   {
      super.connectionOpened(connectionId, message, userData);
   }

   protected void connectionClosed(String connectionId, String message, Object userData)
   {
      super.connectionClosed(connectionId, message, userData);
   }

   protected void connectionFailed(String connectionId, String message, Object userData)
   {
      super.connectionFailed(connectionId, message, userData);
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }
}
