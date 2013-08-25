/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import javax.management.MBeanServerDelegate;

/**
 * The MBeanServerDelegate subclass typical of the MX4J implementation.
 *
 * @version $Revision: 1.12 $
 * @see javax.management.MBeanServerBuilder
 */
public class MX4JMBeanServerDelegate extends MBeanServerDelegate
{
   public String getImplementationName()
   {
      return "MX4J";
   }

   public String getImplementationVendor()
   {
      return "The MX4J Team";
   }

   public String getImplementationVersion()
   {
      return "3.0.2";
   }
}
