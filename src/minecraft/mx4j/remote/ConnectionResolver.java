/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import mx4j.log.Logger;

/**
 * ConnectionResolver handles the details of creating connections for different protocols.
 * Subclasses for the specific protocol are instantiated using a mechanism very similar to the
 * one specified by {@link javax.management.remote.JMXConnectorFactory}. Here a subclass
 * has a fully qualified name specified like this:
 * <package>.resolver.<protocol>.Resolver, for example
 * {@link mx4j.remote.resolver.rmi.Resolver}
 * This class is used from both the client and the server.
 * The former uses it to lookup stubs or connections to the server side; the latter uses it
 * to create server instances and make them availale to clients, for example via JNDI.
 * The client and server methods have not been splitted into 2 different interfaces because
 * most of the times they share common code, although it may have been a better design.
 *
 * @version $Revision: 1.6 $
 */
public abstract class ConnectionResolver extends ProviderHelper
{
   /**
    * Returns a subclass of ConnectionResolver for the specified protocol.
    */
   public static ConnectionResolver newConnectionResolver(String proto, Map environment)
   {
      String protocol = normalizeProtocol(proto);
      String resolverPackages = findResolverPackageList();
      ClassLoader classLoader = findResolverClassLoader(environment, JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER);
      return loadResolver(resolverPackages, protocol, classLoader);
   }

   private static String findResolverPackageList()
   {
      String packages = findSystemPackageList(MX4JRemoteConstants.PROTOCOL_RESOLVER_PACKAGES);
      if (packages == null)
         packages = MX4JRemoteConstants.RESOLVER_PACKAGES;
      else
         packages += MX4JRemoteConstants.RESOLVER_PACKAGES_SEPARATOR + MX4JRemoteConstants.RESOLVER_PACKAGES;
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Resolver packages list is: " + packages);
      return packages;
   }

   private static ClassLoader findResolverClassLoader(Map environment, String loaderKey)
   {
      if (environment == null) return Thread.currentThread().getContextClassLoader();
      Object object = environment.get(loaderKey);
      if (object == null) return Thread.currentThread().getContextClassLoader();
      if (!(object instanceof ClassLoader)) throw new IllegalArgumentException("Environment property " + loaderKey + " must be a ClassLoader");
      return (ClassLoader)object;
   }

   private static ConnectionResolver loadResolver(String packages, String protocol, ClassLoader loader)
   {
      Logger logger = getLogger();

      StringTokenizer tokenizer = new StringTokenizer(packages, MX4JRemoteConstants.RESOLVER_PACKAGES_SEPARATOR);
      while (tokenizer.hasMoreTokens())
      {
         String pkg = tokenizer.nextToken().trim();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Resolver package: " + pkg);
         if (pkg.length() == 0) continue;

         String resolverClassName = constructClassName(pkg, protocol, MX4JRemoteConstants.RESOLVER_CLASS);

         Class resolverClass = null;
         try
         {
            resolverClass = loadClass(resolverClassName, loader);
         }
         catch (ClassNotFoundException x)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Resolver class " + resolverClassName + " not found, continuing with next package");
            continue;
         }
         catch (Exception x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot load resolver class " + resolverClassName, x);
            return null;
         }

         try
         {
            return (ConnectionResolver)resolverClass.newInstance();
         }
         catch (Exception x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot instantiate resolver class " + resolverClassName, x);
            return null;
         }
      }

      // Nothing found
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Could not find resolver for protocol " + protocol + " in package list '" + packages + "'");
      return null;
   }

   /**
    * Looks up a connection with the server side as specified in the given JMXServiceURL.
    * This method is used in implementations of {@link javax.management.remote.JMXConnector#connect()}.
    *
    * @see #bindClient
    */
   public abstract Object lookupClient(JMXServiceURL url, Map environment) throws IOException;

   /**
    * Connects the client returned by {@link #lookupClient} to the server side.
    *
    * @return An object of the same type as the client passed in; normally the client object itself
    */
   public abstract Object bindClient(Object client, Map environment) throws IOException;

   /**
    * Creates an instance of the server as specified in the given JMXServiceURL.
    * It is only a factory method, it should just return a fresh instance of the server;
    * other methods are responsible to make it available to clients (for example exporting it).
    * This method is used in implementations of {@link javax.management.remote.JMXConnectorServer#start}.
    *
    * @see #bindServer
    * @see #destroyServer
    */
   public abstract Object createServer(JMXServiceURL url, Map environment) throws IOException;

   /**
    * Binds the server created by {@link #createServer} to a place specified by the JMXServiceURL.
    *
    * @return a new JMXServiceURL that specifies where the server has been bound to.
    * @see #unbindServer
    */
   public abstract JMXServiceURL bindServer(Object server, JMXServiceURL url, Map environment) throws IOException;

   /**
    * Unbinds the server bound by {@link #bindServer} from the place specified by the JMXServiceURL.
    *
    * @see #destroyServer
    */
   public abstract void unbindServer(Object server, JMXServiceURL address, Map environment) throws IOException;

   /**
    * Destroys the server created by {@link #createServer}, by cleaning up resources it may have requested
    * at creation time
    *
    * @see #createServer
    */
   public abstract void destroyServer(Object server, JMXServiceURL url, Map environment) throws IOException;
}
