/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote.rmi;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.rmi.Remote;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.security.auth.Subject;

import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.remote.MX4JRemoteUtils;

/**
 * @version $Revision: 1.14 $
 */
public abstract class RMIServerImpl implements RMIServer
{
   private ClassLoader defaultClassLoader;
   private MBeanServer server;
   private Map environment;
   private RMIConnectorServer connector;
   private Map connections = new HashMap();
   private final AccessControlContext context;

   public RMIServerImpl(Map environment)
   {
      this.environment = new HashMap(environment);
      this.context = AccessController.getContext();
   }

   AccessControlContext getContext()
   {
      return context;
   }

   public abstract Remote toStub() throws IOException;

   protected abstract void export() throws IOException;

   protected abstract String getProtocol();

   protected abstract RMIConnection makeClient(String connectionId, Subject subject) throws IOException;

   protected abstract void closeClient(RMIConnection client) throws IOException;

   protected abstract void closeServer() throws IOException;

   public ClassLoader getDefaultClassLoader()
   {
      return defaultClassLoader;
   }

   public void setDefaultClassLoader(ClassLoader defaultClassLoader)
   {
      this.defaultClassLoader = defaultClassLoader;
   }

   public synchronized void setMBeanServer(MBeanServer server)
   {
      this.server = server;
   }

   public synchronized MBeanServer getMBeanServer()
   {
      return server;
   }

   public String getVersion()
   {
      return "1.0 MX4J";
   }

   public synchronized RMIConnection newClient(Object credentials) throws IOException, SecurityException
   {
      final Subject subject = authenticate(getEnvironment(), credentials);

      final String connectionId = MX4JRemoteUtils.createConnectionID(getProtocol(), null, -1, subject);

      try
      {
         RMIConnection client = makeClient(connectionId, subject);

         WeakReference weak = new WeakReference(client);

         synchronized (connections)
         {
            connections.put(connectionId, weak);
         }

         connector.connectionOpened(connectionId, "Opened connection " + client, null);

         return client;
      }
      catch (IOException x)
      {
         throw x;
      }
      catch (RuntimeException x)
      {
         throw x;
      }
      catch (Exception x)
      {
         throw new IOException(x.toString());
      }
   }

   private Subject authenticate(Map env, final Object credentials) throws SecurityException
   {
      Logger logger = getLogger();

      Subject subject = null;
      final JMXAuthenticator authenticator = (JMXAuthenticator)env.get(JMXConnectorServer.AUTHENTICATOR);
      if (authenticator != null)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Authenticating new client using JMXAuthenticator " + authenticator);
         try
         {
            // We must check that the code that provided the JMXAuthenticator
            // has the permission to create a Subject
            subject = (Subject)AccessController.doPrivileged(new PrivilegedAction()
            {
               public Object run()
               {
                  return authenticator.authenticate(credentials);
               }
            }, getContext());
            if (subject == null) throw new SecurityException("JMXAuthenticator returned null Subject");
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Authentication successful");
         }
         catch (SecurityException x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Authentication failed", x);
            throw x;
         }
         catch (Throwable x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Authentication failed", x);
            throw new SecurityException(x.toString());
         }
      }
      return subject;
   }

   protected void clientClosed(RMIConnection client) throws IOException
   {
      // Here we arrive when
      // 1. The server is closed
      // 2. The client is closed
      // We must ensure the connection is in both cases removed from the list of active connections

      String connectionID = client.getConnectionId();
      WeakReference weak = null;
      synchronized (connections)
      {
         weak = (WeakReference)connections.remove(connectionID);
      }
      // TODO: maybe I am overzealous here, I could return silently
      if (weak == null) throw new IOException("Could not find active connection with ID " + connectionID);

      RMIConnection connection = (RMIConnection)weak.get();
      if (connection != client) throw new IOException("Could not find active connection " + client);

      closeClient(client);
      connector.connectionClosed(client.getConnectionId(), "Closed connection " + client, null);
   }

   public synchronized void close() throws IOException
   {
      // The process of closing the server does:
      // 1. closeServer() --> unexports the server
      // 2. for each client in connections:
      //    connection.close()
      //       clientClosed(client)
      //           closeClient(client) --> unexports the client

      // The process of closing the client does:
      // 1. connection.close()
      //       clientClosed(client)
      //           closeClient(client) --> unexports the client

      IOException serverException = null;
      try
      {
         closeServer();
      }
      catch (IOException x)
      {
         serverException = x;
      }

      try
      {
         closeConnections();
      }
      catch (IOException x)
      {
         if (serverException != null) throw serverException;
         throw x;
      }
   }

   private void closeConnections() throws IOException
   {
      IOException clientException = null;
      synchronized (connections)
      {
         while (!connections.isEmpty())
         {
            // Yes, create an iterator every time.
            // While expensive, this is needed because connection.close() must
            // be able to modify the connections Map, and we don't want
            // to get ConcurrentModificationExceptions
            Iterator entries = connections.entrySet().iterator();
            Map.Entry entry = (Map.Entry)entries.next();
            WeakReference weak = (WeakReference)entry.getValue();
            RMIConnection connection = (RMIConnection)weak.get();
            if (connection == null)
            {
               // We can use the iterator to remove the entry,
               // since we don't call close(), that modifies the collection
               entries.remove();
               continue;
            }
            else
            {
               try
               {
                  connection.close();
               }
               catch (IOException x)
               {
                  if (clientException == null) clientException = x;
               }
            }
         }
      }
      if (clientException != null) throw clientException;
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   Map getEnvironment()
   {
      return environment;
   }

   void setRMIConnectorServer(RMIConnectorServer cntorServer)
   {
      this.connector = cntorServer;
   }
}
