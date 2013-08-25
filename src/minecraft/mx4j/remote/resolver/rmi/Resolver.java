/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.resolver.rmi;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.MalformedURLException;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Hashtable;
import java.util.Map;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import mx4j.log.Logger;
import mx4j.remote.ConnectionResolver;
import mx4j.util.Base64Codec;

/**
 * Resolver for RMI/JRMP protocol.
 *
 * @version $Revision: 1.3 $
 */
public class Resolver extends ConnectionResolver
{
   private static final String JNDI_CONTEXT = "/jndi/";
   private static final String STUB_CONTEXT = "/stub/";


//********************************************************************************************************************//
// CLIENT METHODS


   public Object lookupClient(JMXServiceURL url, Map environment) throws IOException
   {
      return lookupRMIServerStub(url, environment);
   }

   public Object bindClient(Object client, Map environment) throws IOException
   {
      // JRMP does not need anything special
      return client;
   }

   protected RMIServer lookupRMIServerStub(JMXServiceURL url, Map environment) throws IOException
   {
      Logger logger = getLogger();

      String path = url.getURLPath();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("JMXServiceURL for lookup is: '" + url + "'");

      if (path != null)
      {
         if (path.startsWith(JNDI_CONTEXT))
         {
            return lookupStubInJNDI(url, environment);
         }

         return decodeStub(url, environment);
      }

      throw new MalformedURLException("Unsupported lookup " + url);
   }

   protected RMIServer lookupStubInJNDI(JMXServiceURL url, Map environment) throws IOException
   {
      Logger logger = getLogger();

      String path = url.getURLPath();
      String name = path.substring(JNDI_CONTEXT.length());
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Looking up RMI stub in JNDI under name " + name);

      InitialContext ctx = null;
      try
      {
         ctx = new InitialContext(new Hashtable(environment));
         Object stub = ctx.lookup(name);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Found RMI stub in JNDI " + stub);
         return narrowRMIServerStub(stub);
      }
      catch (NamingException x)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot lookup RMI stub in JNDI", x);
         throw new IOException(x.toString());
      }
      finally
      {
         try
         {
            if (ctx != null) ctx.close();
         }
         catch (NamingException x)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot close InitialContext", x);
         }
      }
   }

   protected RMIServer narrowRMIServerStub(Object stub)
   {
      return (RMIServer)stub;
   }

   protected RMIServer decodeStub(JMXServiceURL url, Map environment) throws IOException
   {
      String path = url.getURLPath();
      if (path.startsWith(STUB_CONTEXT))
      {
         byte[] encoded = path.substring(STUB_CONTEXT.length()).getBytes();
         if (!Base64Codec.isArrayByteBase64(encoded)) throw new IOException("Encoded stub form is not a valid Base64 sequence: " + url);
         byte[] decoded = Base64Codec.decodeBase64(encoded);
         ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
         ObjectInputStream ois = null;
         try
         {
            ois = new ObjectInputStream(bais);
            return (RMIServer)ois.readObject();
         }
         catch (ClassNotFoundException x)
         {
            throw new IOException("Cannot decode stub from " + url + ": " + x);
         }
         finally
         {
            if (ois != null) ois.close();
         }
      }
      throw new MalformedURLException("Unsupported binding: " + url);
   }


//********************************************************************************************************************//
// SERVER METHODS


   public Object createServer(JMXServiceURL url, Map environment) throws IOException
   {
      return createRMIServer(url, environment);
   }

   protected RMIServerImpl createRMIServer(JMXServiceURL url, Map environment) throws IOException
   {
      int port = url.getPort();
      RMIClientSocketFactory clientFactory = (RMIClientSocketFactory)environment.get(RMIConnectorServer.RMI_CLIENT_SOCKET_FACTORY_ATTRIBUTE);
      RMIServerSocketFactory serverFactory = (RMIServerSocketFactory)environment.get(RMIConnectorServer.RMI_SERVER_SOCKET_FACTORY_ATTRIBUTE);
      return new RMIJRMPServerImpl(port, clientFactory, serverFactory, environment);
   }

   public JMXServiceURL bindServer(Object server, JMXServiceURL url, Map environment) throws IOException
   {
      // See javax/management/remote/rmi/package-summary.html

      RMIServerImpl rmiServer = (RMIServerImpl)server;

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("JMXServiceURL for binding is: '" + url + "'");

      if (isEncodedForm(url))
      {
         String path = encodeStub(rmiServer, environment);
         return new JMXServiceURL(url.getProtocol(), url.getHost(), url.getPort(), path);
      }
      else
      {
         String jndiURL = parseJNDIForm(url);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("JMXServiceURL path for binding is: '" + jndiURL + "'");

         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext(new Hashtable(environment));
            boolean rebind = Boolean.valueOf((String)environment.get(RMIConnectorServer.JNDI_REBIND_ATTRIBUTE)).booleanValue();
            if (rebind)
               ctx.rebind(jndiURL, rmiServer.toStub());
            else
               ctx.bind(jndiURL, rmiServer.toStub());
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Bound " + rmiServer + " to " + jndiURL);
            return url;
         }
         catch (NamingException x)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot bind server " + rmiServer + " to " + jndiURL, x);
            throw new IOException(x.toString());
         }
         finally
         {
            try
            {
               if (ctx != null) ctx.close();
            }
            catch (NamingException x)
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot close InitialContext", x);
            }
         }
      }
   }

   protected String encodeStub(RMIServerImpl rmiServer, Map environment) throws IOException
   {
      Remote stub = rmiServer.toStub();
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = null;
      try
      {
         oos = new ObjectOutputStream(baos);
         oos.writeObject(stub);
      }
      finally
      {
         if (oos != null) oos.close();
      }
      byte[] bytes = baos.toByteArray();
      byte[] encoded = Base64Codec.encodeBase64(bytes);
      // Since the bytes are base 64 bytes, the encoding in creating the string is not important: any will work
      return STUB_CONTEXT + new String(encoded);
   }

   protected boolean isEncodedForm(JMXServiceURL url)
   {
      String path = url.getURLPath();
      if (path == null || path.length() == 0 || "/".equals(path) || path.startsWith(STUB_CONTEXT)) return true;
      return false;
   }

   private String parseJNDIForm(JMXServiceURL url) throws MalformedURLException
   {
      String path = url.getURLPath();
      if (path.startsWith(JNDI_CONTEXT))
      {
         String jndiURL = path.substring(JNDI_CONTEXT.length());
         if (jndiURL == null || jndiURL.length() == 0) throw new MalformedURLException("No JNDI URL specified: " + url);
         return jndiURL;
      }
      throw new MalformedURLException("Unsupported binding: " + url);
   }

   public void unbindServer(Object server, JMXServiceURL url, Map environment) throws IOException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("JMXServiceURL for unbinding is: '" + url + "'");

      // The server was not bound to JNDI (the stub was encoded), just return
      if (isEncodedForm(url))
      {
         return;
      }
      else
      {
         String jndiURL = parseJNDIForm(url);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("JMXServiceURL path for unbinding is: '" + jndiURL + "'");

         InitialContext ctx = null;
         try
         {
            ctx = new InitialContext(new Hashtable(environment));
            ctx.unbind(jndiURL);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Unbound " + server + " from " + jndiURL);
         }
         catch (NamingException x)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot unbind server " + server + " to " + jndiURL, x);
            throw new IOException(x.toString());
         }
         finally
         {
            try
            {
               if (ctx != null) ctx.close();
            }
            catch (NamingException x)
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Cannot close InitialContext", x);
            }
         }
      }
   }

   public void destroyServer(Object server, JMXServiceURL url, Map environment) throws IOException
   {
   }
}
