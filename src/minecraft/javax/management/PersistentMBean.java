/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;


/**
 * This interface is implemented by MBeans that are able to make themselves persistent.
 *
 * @version $Revision: 1.6 $
 */
public interface PersistentMBean
{
   /**
    * Loads a previously saved MBean state into the MBean itself.
    *
    * @throws MBeanException            If an exception occurred during loading or if loading is not supported.
    * @throws InstanceNotFoundException If some MBean needed for loading is not found.
    * @see #store
    */
   public void load() throws MBeanException, InstanceNotFoundException, RuntimeOperationsException;

   /**
    * Stores the MBean state into a persistent media.
    *
    * @throws MBeanException            If an exception occurred during storing or if storing is not supported.
    * @throws InstanceNotFoundException If some MBean needed for storing is not found.
    */
   public void store() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;
}
