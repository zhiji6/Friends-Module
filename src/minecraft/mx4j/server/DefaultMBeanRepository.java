/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.util.HashMap;
import java.util.Iterator;
import javax.management.ObjectName;

/**
 * Default implementation of the MBeanRepository interface.
 *
 * @version $Revision: 1.5 $
 */
class DefaultMBeanRepository implements MBeanRepository
{
   private HashMap m_map = new HashMap();

   public MBeanMetaData get(ObjectName name)
   {
      return (MBeanMetaData)m_map.get(name);
   }

   public void put(ObjectName name, MBeanMetaData metadata)
   {
      m_map.put(name, metadata);
   }

   public void remove(ObjectName name)
   {
      m_map.remove(name);
   }

   public int size()
   {
      return m_map.size();
   }

   public Iterator iterator()
   {
      return m_map.values().iterator();
   }

   public Object clone()
   {
      try
      {
         DefaultMBeanRepository repository = (DefaultMBeanRepository)super.clone();
         repository.m_map = (HashMap)m_map.clone();
         return repository;
      }
      catch (CloneNotSupportedException ignored)
      {
         return null;
      }
   }
}
