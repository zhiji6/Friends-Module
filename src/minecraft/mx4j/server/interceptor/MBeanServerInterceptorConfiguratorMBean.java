/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import javax.management.MBeanException;
import javax.management.ObjectName;

/**
 * Management interface for the MBeanServerInterceptorConfigurator MBean.
 *
 * @version $Revision: 1.6 $
 */
public interface MBeanServerInterceptorConfiguratorMBean
{
   /**
    * Appends the given interceptor, provided by the client, to the existing interceptor chain.
    *
    * @see #registerInterceptor
    */
   public void addInterceptor(MBeanServerInterceptor interceptor);

   /**
    * Appends the given interceptor, provided by the client, to the existing interceptor chain and registers it as MBean.
    *
    * @see #addInterceptor
    */
   public void registerInterceptor(MBeanServerInterceptor interceptor, ObjectName name) throws MBeanException;

   /**
    * Removes all the interceptors added via {@link #addInterceptor(MBeanServerInterceptor interceptor)}.
    *
    * @see #addInterceptor
    */
   public void clearInterceptors();

   /**
    * Starts this configurator, so that the MBeanServer is now able to accept incoming calls.
    *
    * @see #stop
    * @see #isRunning
    */
   public void start();

   /**
    * Stops this configurator, so that the MBeanServer is not able to accept incoming calls.
    *
    * @see #start
    */
   public void stop();

   /**
    * Returns whether this configurator is running and thus if the MBeanServer can accept incoming calls
    *
    * @see #start
    */
   public boolean isRunning();
}
