/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import javax.management.MBeanServer;

/**
 * @version $Revision: 1.3 $
 */
public interface MBeanServerForwarder extends MBeanServer
{
   public MBeanServer getMBeanServer();

   public void setMBeanServer(MBeanServer server) throws IllegalArgumentException;
}
