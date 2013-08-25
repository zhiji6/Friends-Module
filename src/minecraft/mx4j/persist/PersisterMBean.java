/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.persist;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

/**
 * Management interface for components able to persist information to a storage media.
 *
 * @version $Revision: 1.6 $
 */
public interface PersisterMBean
{
   /**
    * Loads the information persisted on the storage media.
    *
    * @see #store
    */
   public Object load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;

   /**
    * Store the given information to the storage media.
    *
    * @see #load
    */
   public void store(Object data) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException;
}
