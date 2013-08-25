/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import mx4j.remote.ProviderFactory;

/**
 * @version $Revision: 1.11 $
 */
public class JMXConnectorFactory
{
   public static final String DEFAULT_CLASS_LOADER = "jmx.remote.default.class.loader";
   public static final String PROTOCOL_PROVIDER_PACKAGES = "jmx.remote.protocol.provider.pkgs";
   public static final String PROTOCOL_PROVIDER_CLASS_LOADER = "jmx.remote.protocol.provider.class.loader";

   private JMXConnectorFactory()
   {
   }

   public static JMXConnector connect(JMXServiceURL url) throws IOException
   {
      return connect(url, null);
   }

   public static JMXConnector connect(JMXServiceURL url, Map environment) throws IOException
   {
      JMXConnector connector = newJMXConnector(url, environment);
      connector.connect(environment);
      return connector;
   }

   public static JMXConnector newJMXConnector(JMXServiceURL url, Map env) throws IOException
   {
      Map envCopy = env == null ? new HashMap() : new HashMap(env);
      JMXConnector connector = ProviderFactory.newJMXConnector(url, envCopy);
      return connector;
   }
}
