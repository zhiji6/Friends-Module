/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;

/**
 * @version $Revision: 1.12 $
 */
public class JMXServiceURL implements Serializable
{
   private static final long serialVersionUID = 8173364409860779292l;

   /**
    * @serial The protocol
    */
   private String protocol;
   /**
    * @serial The host
    */
   private String host;
   /**
    * @serial The port
    */
   private int port;
   /**
    * @serial The path
    */
   private String urlPath;

   private transient int hash;

   public JMXServiceURL(String url) throws MalformedURLException
   {
      if (url == null) throw new NullPointerException("Null JMXServiceURL string");
      parse(url);
   }

   public JMXServiceURL(String protocol, String host, int port) throws MalformedURLException
   {
      this(protocol, host, port, null);
   }

   public JMXServiceURL(String protocol, String host, int port, String urlPath) throws MalformedURLException
   {
      if (port < 0) throw new MalformedURLException("Port number cannot be less than zero");

      setProtocol(protocol);
      setHost(host);
      setPort(port);
      setURLPath(urlPath);
   }

   private String resolveHost() throws MalformedURLException
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (UnknownHostException x)
      {
         throw new MalformedURLException("Cannot resolve local host name");
      }
   }

   public String getProtocol()
   {
      return protocol;
   }

   private void setProtocol(String protocol)
   {
      if (protocol != null)
         this.protocol = protocol.toLowerCase();
      else
         this.protocol = "jmxmp"; // Default required by the spec
   }

   public String getHost()
   {
      return host;
   }

   private void setHost(String host) throws MalformedURLException
   {
      if (host != null)
         this.host = host.toLowerCase();
      else
         this.host = resolveHost().toLowerCase(); // Default required by the spec
   }

   public int getPort()
   {
      return port;
   }

   private void setPort(int port)
   {
      this.port = port;
   }

   public String getURLPath()
   {
      return urlPath;
   }

   private void setURLPath(String urlPath)
   {
      if (urlPath != null)
      {
         if (urlPath.length() > 0 && !urlPath.startsWith("/")) urlPath = "/" + urlPath;
         this.urlPath = urlPath;
      }
      else
      {
         this.urlPath = ""; // Default required by the spec
      }
   }

   public int hashCode()
   {
      if (hash == 0)
      {
         hash = getProtocol().hashCode();
         String host = getHost();
         hash = 29 * hash + (host != null ? host.hashCode() : 0);
         hash = 29 * hash + getPort();
         String path = getURLPath();
         hash = 29 * hash + (path != null ? path.hashCode() : 0);
      }
      return hash;
   }

   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (!(obj instanceof JMXServiceURL)) return false;

      JMXServiceURL other = (JMXServiceURL)obj;

      if (!getProtocol().equalsIgnoreCase(other.getProtocol())) return false;

      String host = getHost();
      String otherHost = other.getHost();
      if (host != null ? !host.equalsIgnoreCase(otherHost) : otherHost != null) return false;

      if (getPort() != other.getPort()) return false;

      String path = getURLPath();
      String otherPath = other.getURLPath();
      if (path != null ? !path.equals(otherPath) : otherPath != null) return false;

      return true;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("service:jmx:");
      buffer.append(getProtocol()).append("://");
      buffer.append(getHost());
      int port = getPort();
      if (port > 0) buffer.append(":").append(port);
      String path = getURLPath();
      if (path != null)
      {
         if (!path.startsWith("/")) buffer.append("/");
         buffer.append(path);
      }
      return buffer.toString();
   }

   private void parse(String url) throws MalformedURLException
   {
      String prefix = "service:jmx:";
      if (url.length() <= prefix.length()) throw new MalformedURLException("JMXServiceURL " + url + " must start with " + prefix);
      String servicejmx = url.substring(0, prefix.length());
      if (!servicejmx.equalsIgnoreCase(prefix)) throw new MalformedURLException("JMXServiceURL " + url + " must start with " + prefix);

      String parse = url.substring(prefix.length());

      String hostSeparator = "://";
      int index = parse.indexOf(hostSeparator);
      if (index < 0) throw new MalformedURLException("No protocol defined for JMXServiceURL " + url);
      String protocol = parse.substring(0, index);
      checkProtocol(url, protocol);
      setProtocol(protocol);

      String hostAndMore = parse.substring(index + hostSeparator.length());
      index = hostAndMore.indexOf('/');
      if (index < 0)
      {
         parseHostAndPort(url, hostAndMore);
         setURLPath(null);
      }
      else
      {
         String hostAndPort = hostAndMore.substring(0, index);
         parseHostAndPort(url, hostAndPort);

         String pathAndMore = hostAndMore.substring(index);
         if (pathAndMore.length() > 0)
         {
            checkURLPath(url, pathAndMore);
            String path = "/".equals(pathAndMore) ? "" : pathAndMore; // Special case
            setURLPath(path);
         }
      }
   }

   private void parseHostAndPort(String url, String hostAndPort) throws MalformedURLException
   {
      if (hostAndPort.length() == 0)
      {
         setHost(null);
         setPort(0);
         return;
      }

      int colon = hostAndPort.indexOf(':');
      if (colon == 0) throw new MalformedURLException("No host defined for JMXServiceURL " + url);

      if (colon > 0)
      {
         String host = hostAndPort.substring(0, colon);
         checkHost(url, host);
         setHost(host);
         String portString = hostAndPort.substring(colon + 1);
         try
         {
            int port = Integer.parseInt(portString);
            setPort(port);
         }
         catch (NumberFormatException x)
         {
            throw new MalformedURLException("Invalid port " + portString + " for JMXServiceURL " + url);
         }
      }
      else
      {
         checkHost(url, hostAndPort);
         setHost(hostAndPort);
         setPort(0);
      }
   }

   private void checkProtocol(String url, String protocol) throws MalformedURLException
   {
      if (protocol.length() == 0) throw new MalformedURLException("No protocol defined for JMXServiceURL " + url);
      if (!protocol.trim().equals(protocol)) throw new MalformedURLException("No leading or trailing white space allowed in protocol for JMXServiceURL " + url);
   }

   private void checkHost(String url, String host) throws MalformedURLException
   {
      if (host.length() == 0) throw new MalformedURLException("No host defined for JMXServiceURL " + url);
      if (!host.trim().equals(host)) throw new MalformedURLException("No leading or trailing white space allowed in host for JMXServiceURL " + url);
   }

   private void checkURLPath(String url, String path) throws MalformedURLException
   {
      if (!path.startsWith("/")) throw new MalformedURLException("Invalid path for JMXServiceURL " + url);
      if (!path.trim().equals(path)) throw new MalformedURLException("No leading or trailing white space allowed in path for JMXServiceURL " + url);
   }
}
