/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

/**
 * Management interface for the SecurityMBeanServerInterceptor MBean
 *
 * @version $Revision: 1.3 $
 */
public interface SecurityMBeanServerInterceptorMBean
{
   /**
    * Returns the type of this interceptor
    */
   public String getType();

   /**
    * This interceptor is always enabled
    */
   public boolean isEnabled();
}
