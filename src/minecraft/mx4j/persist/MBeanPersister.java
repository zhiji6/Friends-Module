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
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.RuntimeOperationsException;

/**
 * A persister that delegates the persistence to a registered persister MBean.
 *
 * @version $Revision: 1.7 $
 */
public class MBeanPersister extends Persister
{
   private MBeanServer m_server;
   private ObjectName m_name;
   private PersisterMBean m_proxy;

   /**
    * Creates a new MBeanPersister that delegates persistence to a persister MBean
    * registered in the specified MBeanServer with the specified ObjectName.
    */
   public MBeanPersister(MBeanServer server, ObjectName name)
   {
      m_server = server;
      m_name = name;
      m_proxy = (PersisterMBean)MBeanServerInvocationHandler.newProxyInstance(server, name, PersisterMBean.class, false);
   }

   public Object load() throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      return m_proxy.load();
   }

   public void store(Object data) throws MBeanException, RuntimeOperationsException, InstanceNotFoundException
   {
      m_proxy.store(data);
   }
}
