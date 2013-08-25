/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import mx4j.remote.ConnectionNotificationEmitter;
import mx4j.remote.ConnectionResolver;
import mx4j.remote.HeartBeat;
import mx4j.remote.RemoteNotificationClientHandler;
import mx4j.remote.rmi.ClientExceptionCatcher;
import mx4j.remote.rmi.ClientInvoker;
import mx4j.remote.rmi.ClientUnmarshaller;
import mx4j.remote.rmi.RMIHeartBeat;
import mx4j.remote.rmi.RMIRemoteNotificationClientHandler;

/**
 * @version $Revision: 1.26 $
 */
public class RMIConnector implements JMXConnector, Serializable
{
   private static final long serialVersionUID = 817323035842634473l;

   /**
    * @serial
    */
   private final JMXServiceURL jmxServiceURL;
   /**
    * @serial
    */
   private RMIServer rmiServer;

   private transient RMIConnection connection;
   private transient boolean connected;
   private transient boolean closed;
   private transient ClassLoader defaultClassLoader;
   private transient String connectionId;
   private transient ConnectionNotificationEmitter emitter;
   private transient HeartBeat heartbeat;
   private transient RemoteNotificationClientHandler notificationHandler;

   public RMIConnector(JMXServiceURL url, Map environment)
   {
      if (url == null) throw new IllegalArgumentException("JMXServiceURL cannot be null");
      this.jmxServiceURL = url;
      this.rmiServer = null;
      initialize(environment);
   }

   public RMIConnector(RMIServer server, Map environment)
   {
      if (server == null) throw new IllegalArgumentException("RMIServer cannot be null");
      this.jmxServiceURL = null;
      this.rmiServer = server;
      initialize(environment);
   }

   private void initialize(Map environment)
   {
      this.defaultClassLoader = findDefaultClassLoader(environment);
      this.emitter = new ConnectionNotificationEmitter(this);
   }

   private ClassLoader findDefaultClassLoader(Map environment)
   {
      if (environment == null) return null;
      Object loader = environment.get(JMXConnectorFactory.DEFAULT_CLASS_LOADER);
      if (loader != null && !(loader instanceof ClassLoader)) throw new IllegalArgumentException("Environment property " + JMXConnectorFactory.DEFAULT_CLASS_LOADER + " must be a ClassLoader");
      return (ClassLoader)loader;
   }

   public void connect() throws IOException, SecurityException
   {
      connect(null);
   }

   public void connect(Map environment) throws IOException, SecurityException
   {
      synchronized (this)
      {
         if (connected) return;
         if (closed) throw new IOException("This RMIConnector has already been closed");

         // Spec says, about default ClassLoader, to look here:
         // 1. Environment at connect(); if null,
         // 2. Environment at creation; if null,
         // 3. Context classloader at connect().
         ClassLoader loader = findDefaultClassLoader(environment);
         if (loader != null)
            defaultClassLoader = loader;
         else if (defaultClassLoader == null)
            defaultClassLoader = Thread.currentThread().getContextClassLoader();

         Map env = environment == null ? new HashMap() : environment;

         String protocol = jmxServiceURL.getProtocol();
         ConnectionResolver resolver = ConnectionResolver.newConnectionResolver(protocol, env);
         if (resolver == null) throw new IOException("Unsupported protocol: " + protocol);
         if (rmiServer == null) rmiServer = (RMIServer)resolver.lookupClient(jmxServiceURL, env);
         rmiServer = (RMIServer)resolver.bindClient(rmiServer, env);

         Object credentials = env.get(CREDENTIALS);
         this.connection = rmiServer.newClient(credentials);

         connected = true;
         this.connectionId = connection.getConnectionId();

         this.heartbeat = new RMIHeartBeat(connection, emitter, env);
         this.notificationHandler = new RMIRemoteNotificationClientHandler(connection, defaultClassLoader, emitter, heartbeat, env);

         this.heartbeat.start();
         this.notificationHandler.start();
      }

      emitter.sendConnectionNotificationOpened();
   }

   public void close() throws IOException
   {
      synchronized (this)
      {
         if (closed) return;
         connected = false;
         closed = true;

         if (notificationHandler != null) notificationHandler.stop();
         if (heartbeat != null) heartbeat.stop();
         if (connection != null) connection.close();

         connection = null;
         rmiServer = null;
      }

      emitter.sendConnectionNotificationClosed();
   }

   public String getConnectionId() throws IOException
   {
      return connectionId;
   }

   public MBeanServerConnection getMBeanServerConnection() throws IOException
   {
      return getMBeanServerConnection(null);
   }

   public MBeanServerConnection getMBeanServerConnection(Subject delegate) throws IOException
   {
      if (!connected) throw new IOException("Connection has not been established");

      // TODO: here we hardcode the client invocation chain. Maybe worth remove this hardcoding ?
      ClientInvoker invoker = new ClientInvoker(connection, notificationHandler, delegate);
      MBeanServerConnection unmarshaller = ClientUnmarshaller.newInstance(invoker, defaultClassLoader);
      MBeanServerConnection catcher = ClientExceptionCatcher.newInstance(unmarshaller);

      return catcher;
   }

   public void addConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      emitter.addNotificationListener(listener, filter, handback);
   }

   public void removeConnectionNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener);
   }

   public void removeConnectionNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener, filter, handback);
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
   {
      ois.defaultReadObject();
      if (jmxServiceURL == null && rmiServer == null) throw new InvalidObjectException("Nor the JMXServiceURL nor the RMIServer were specified for this RMIConnector");
      initialize(null);
   }

   private void writeObject(ObjectOutputStream oos) throws IOException
   {
      if (jmxServiceURL == null && rmiServer == null) throw new InvalidObjectException("Nor the JMXServiceURL nor the RMIServer were specified for this RMIConnector");
      oos.defaultWriteObject();
   }
}
