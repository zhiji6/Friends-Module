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

import javax.management.MBeanServer;

import mx4j.remote.ProviderFactory;

/**
 * @version $Revision: 1.7 $
 */
public class JMXConnectorServerFactory
{
   public static final String DEFAULT_CLASS_LOADER = "jmx.remote.default.class.loader";
   public static final String DEFAULT_CLASS_LOADER_NAME = "jmx.remote.default.class.loader.name";
   public static final String PROTOCOL_PROVIDER_PACKAGES = "jmx.remote.protocol.provider.pkgs";
   public static final String PROTOCOL_PROVIDER_CLASS_LOADER = "jmx.remote.protocol.provider.class.loader";

   private JMXConnectorServerFactory()
   {
   }

   public static JMXConnectorServer newJMXConnectorServer(JMXServiceURL url, Map environment, MBeanServer server) throws IOException
   {
      Map env = environment == null ? new HashMap() : new HashMap(environment);
      JMXConnectorServer connector = ProviderFactory.newJMXConnectorServer(url, env, server);
      return connector;
   }
}
