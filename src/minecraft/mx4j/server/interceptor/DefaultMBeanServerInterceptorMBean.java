/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

/**
 * Management interface for the DefaultMBeanServerInterceptor MBean
 *
 * @version $Revision: 1.6 $
 */
public interface DefaultMBeanServerInterceptorMBean
{
   /**
    * Returns whether this interceptor is enabled
    *
    * @see #setEnabled
    */
   public boolean isEnabled();

   /**
    * Enables or disables this interceptor
    *
    * @see #isEnabled
    */
   public void setEnabled(boolean enabled);

   /**
    * Returns the type of this interceptor
    */
   public String getType();
}
