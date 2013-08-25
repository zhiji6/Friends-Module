/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.util.Iterator;
import javax.management.ObjectName;

/**
 * The MBeanServer implementation delegates to implementations of this interface the storage of registered MBeans. <p>
 * All necessary synchronization code is taken care by the MBeanServer, so implementations can be coded without caring
 * of synchronization issues.
 *
 * @version $Revision: 1.6 $
 */
public interface MBeanRepository extends Cloneable
{
   /**
    * Returns the metadata information associated with the given object name.
    *
    * @see #put
    */
   public MBeanMetaData get(ObjectName name);

   /**
    * Inserts the given metadata associated with the given object name into this repository.
    *
    * @see #get
    */
   public void put(ObjectName name, MBeanMetaData metadata);

   /**
    * Removes the metadata associated with the given object name from this repository.
    */
   public void remove(ObjectName name);

   /**
    * Returns the size of this repository.
    */
   public int size();

   /**
    * Returns an iterator on the metadata stored in this repository.
    */
   public Iterator iterator();

   /**
    * Clones this MBean repository
    */
   public Object clone();
}
