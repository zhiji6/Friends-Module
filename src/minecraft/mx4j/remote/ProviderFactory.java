/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXConnectorProvider;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXConnectorServerProvider;
import javax.management.remote.JMXProviderException;
import javax.management.remote.JMXServiceURL;

import mx4j.log.Logger;

/**
 * @version $Revision: 1.8 $
 */
public class ProviderFactory extends ProviderHelper
{
   public static JMXConnector newJMXConnector(JMXServiceURL url, Map env) throws IOException
   {
      // Yes, throw NPE if url is null (spec compliant)
      String protocol = normalizeProtocol(url.getProtocol());
      String providerPackages = findProviderPackageList(env, JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES);
      ClassLoader classLoader = findProviderClassLoader(env, JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER);
      List providers = loadProviders(JMXConnectorProvider.class, providerPackages, protocol, MX4JRemoteConstants.CLIENT_PROVIDER_CLASS, classLoader);
      for (int i = 0; i < providers.size(); i++)
      {
         JMXConnectorProvider provider = (JMXConnectorProvider)providers.get(i);
         try
         {
            return provider.newJMXConnector(url, Collections.unmodifiableMap(env));
         }
         catch (JMXProviderException x)
         {
            throw x;
         }
         catch (IOException x)
         {
            continue;
         }
      }
      throw new MalformedURLException("Could not find provider for protocol " + protocol);
   }

   public static JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map env, MBeanServer server) throws IOException
   {
      // Yes, throw NPE if url is null (spec compliant)
      String protocol = normalizeProtocol(url.getProtocol());
      String providerPackages = findProviderPackageList(env, JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES);
      ClassLoader classLoader = findProviderClassLoader(env, JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER);
      List providers = loadProviders(JMXConnectorServerProvider.class, providerPackages, protocol, MX4JRemoteConstants.SERVER_PROVIDER_CLASS, classLoader);
      for (int i = 0; i < providers.size(); i++)
      {
         JMXConnectorServerProvider provider = (JMXConnectorServerProvider)providers.get(i);
         try
         {
            return provider.newJMXConnectorServer(url, Collections.unmodifiableMap(env), server);
         }
         catch (JMXProviderException x)
         {
            throw x;
         }
         catch (IOException x)
         {
            continue;
         }
      }
      throw new MalformedURLException("Could not find provider for protocol " + protocol);
   }

   /**
    * public static JMXConnectorProvider newJMXConnectorProvider(JMXServiceURL url, Map env) throws IOException
    * {
    * // Yes, throw NPE if url is null (spec compliant)
    * String protocol = normalizeProtocol(url.getProtocol());
    * String providerPackages = findProviderPackageList(env, JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES);
    * ClassLoader classLoader = findProviderClassLoader(env, JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER);
    * JMXConnectorProvider provider = (JMXConnectorProvider)loadProvider(JMXConnectorProvider.class, providerPackages, protocol, MX4JRemoteConstants.CLIENT_PROVIDER_CLASS, classLoader);
    * return provider;
    * }
    * <p/>
    * public static JMXConnectorServerProvider newJMXConnectorServerProvider(JMXServiceURL url, Map env) throws IOException
    * {
    * // Yes, throw NPE if url is null (spec compliant)
    * String protocol = normalizeProtocol(url.getProtocol());
    * String providerPackages = findProviderPackageList(env, JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES);
    * ClassLoader classLoader = findProviderClassLoader(env, JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER);
    * JMXConnectorServerProvider provider = (JMXConnectorServerProvider)loadProvider(JMXConnectorServerProvider.class, providerPackages, protocol, MX4JRemoteConstants.SERVER_PROVIDER_CLASS, classLoader);
    * return provider;
    * }
    */
   private static String findEnvironmentProviderPackageList(Map environment, String key) throws JMXProviderException
   {
      String providerPackages = null;
      if (environment != null)
      {
         Logger logger = getLogger();
         Object pkgs = environment.get(key);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider packages in the environment: " + pkgs);
         if (pkgs != null && !(pkgs instanceof String)) throw new JMXProviderException("Provider package list must be a string");
         providerPackages = (String)pkgs;
      }
      return providerPackages;
   }

   private static String findProviderPackageList(Map environment, final String providerPkgsKey) throws JMXProviderException
   {
      // 1. Look in the environment
      // 2. Look for system property
      // 3. Use implementation's provider

      String providerPackages = findEnvironmentProviderPackageList(environment, providerPkgsKey);

      if (providerPackages == null)
      {
         providerPackages = findSystemPackageList(providerPkgsKey);
      }

      if (providerPackages != null && providerPackages.trim().length() == 0) throw new JMXProviderException("Provider package list cannot be an empty string");

      if (providerPackages == null)
         providerPackages = MX4JRemoteConstants.PROVIDER_PACKAGES;
      else
         providerPackages += MX4JRemoteConstants.PROVIDER_PACKAGES_SEPARATOR + MX4JRemoteConstants.PROVIDER_PACKAGES;

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider packages list is: " + providerPackages);

      return providerPackages;
   }

   private static ClassLoader findProviderClassLoader(Map environment, String providerLoaderKey)
   {
      Logger logger = getLogger();

      ClassLoader classLoader = null;
      if (environment != null)
      {
         Object loader = environment.get(providerLoaderKey);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider classloader in the environment: " + loader);
         if (loader != null && !(loader instanceof ClassLoader)) throw new IllegalArgumentException("Provider classloader is not a ClassLoader");
         classLoader = (ClassLoader)loader;
      }

      if (classLoader == null)
      {
         classLoader = Thread.currentThread().getContextClassLoader();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider classloader in the environment: " + classLoader);
      }

      // Add the classloader as required by the spec
      environment.put(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER, classLoader);
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Provider classloader added to the environment");

      return classLoader;
   }

   private static List loadProviders(Class providerType, String packages, String protocol, String className, ClassLoader loader) throws JMXProviderException
   {
      Logger logger = getLogger();
      List result = new ArrayList();

      StringTokenizer tokenizer = new StringTokenizer(packages, MX4JRemoteConstants.PROVIDER_PACKAGES_SEPARATOR);
      while (tokenizer.hasMoreTokens())
      {
         String pkg = tokenizer.nextToken().trim();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider package: " + pkg);

         // The spec states the package cannot be empty
         if (pkg.length() == 0) throw new JMXProviderException("Empty package list not allowed: " + packages);

         String providerClassName = constructClassName(pkg, protocol, className);

         Class providerClass = null;
         try
         {
            providerClass = loadClass(providerClassName, loader);
         }
         catch (ClassNotFoundException x)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Provider class " + providerClassName + " not found, " + (tokenizer.hasMoreTokens() ? "continuing with next package" : "no more packages to try"));
            continue;
         }
         catch (Exception x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot load provider class " + providerClassName, x);
            throw new JMXProviderException("Cannot load provider class " + providerClassName, x);
         }

         try
         {
            Object provider = providerClass.newInstance();
            result.add(provider);
         }
         catch (Exception x)
         {
            if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Cannot instantiate provider class " + providerClassName, x);
            throw new JMXProviderException("Cannot instantiate provider class " + providerClassName, x);
         }
      }

      try
      {
         List serviceProviders = fromServiceProviders(providerType, protocol, className, loader);
         result.addAll(serviceProviders);
      }
      catch (IOException x)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Error retrieving service providers", x);
      }

      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Providers found are: " + result);
      return result;
   }

   private static List fromServiceProviders(Class providerType, String protocol, String className, ClassLoader loader) throws IOException
   {
      String services = "META-INF/services/";

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Loading providers from " + services);

      if (loader == null) loader = Thread.currentThread().getContextClassLoader();
      if (loader == null) loader = ClassLoader.getSystemClassLoader();
      Enumeration providerURLs = loader.getResources(services + providerType.getName());
      List providers = new ArrayList();
      while (providerURLs.hasMoreElements())
      {
         final URL providerURL = (URL)providerURLs.nextElement();

         InputStream stream = null;
         try
         {
            stream = (InputStream)AccessController.doPrivileged(new PrivilegedExceptionAction()
            {
               public Object run() throws Exception
               {
                  return providerURL.openStream();
               }
            });
         }
         catch (PrivilegedActionException x)
         {
            Exception xx = x.getException();
            if (xx instanceof IOException) throw (IOException)xx;
            throw new IOException(xx.toString());
         }

         BufferedReader reader = null;
         try
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Reading provider from " + providerURL);
            reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String line = null;
            while ((line = reader.readLine()) != null)
            {
               int comment = line.indexOf('#');
               if (comment >= 0) line = line.substring(0, comment);
               line = line.trim();
               if (line.length() == 0) continue;
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Found provider '" + line + "' in " + providerURL);
               try
               {
                  Class providerClass = loader.loadClass(line);
                  if (providerType.isAssignableFrom(providerClass))
                  {
                     Object providerInstance = providerClass.newInstance();
                     providers.add(providerInstance);
                  }
               }
               catch (Exception ignored)
               {
                  // Skip this line and continue
               }
            }
         }
         finally
         {
            if (reader != null) reader.close();
         }
      }

      return providers;
   }
}
