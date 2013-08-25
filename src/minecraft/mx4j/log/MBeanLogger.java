/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.log;

import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ServiceNotFoundException;

/**
 * This logger forwards log requests to an MBean, that must have an operation with signature
 * <pre>
 * public void log(int priority, Object message, Throwable exception);
 * </pre>
 * It's used by the ModelMBean implementation. <br>
 * Since the constructor takes parameters, cannot be used as prototype for logging redirection.
 *
 * @version $Revision: 1.7 $
 */
public class MBeanLogger extends Logger
{
   private MBeanServer m_server;
   private ObjectName m_name;

   public MBeanLogger(MBeanServer server, ObjectName objectName) throws MBeanException
   {
      if (server == null)
      {
         throw new MBeanException(new IllegalArgumentException("MBeanServer cannot be null"));
      }
      if (objectName == null)
      {
         throw new MBeanException(new IllegalArgumentException("ObjectName cannot be null"));
      }

      m_server = server;
      m_name = objectName;

      boolean found = false;
      try
      {
         MBeanInfo info = m_server.getMBeanInfo(m_name);
         MBeanOperationInfo[] opers = info.getOperations();
         if (opers != null)
         {
            for (int i = 0; i < opers.length; ++i)
            {
               MBeanOperationInfo oper = opers[i];
               if (oper.getName().equals("log"))
               {
                  MBeanParameterInfo[] params = oper.getSignature();
                  if (params.length == 3)
                  {
                     if (params[0].getType().equals("int") &&
                         params[1].getType().equals("java.lang.Object") &&
                         params[2].getType().equals("java.lang.Throwable"))
                     {
                        found = true;
                        break;
                     }
                  }
               }
            }
         }
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
      if (!found)
      {
         throw new MBeanException(new ServiceNotFoundException("MBean does not have an operation log(int,Object,Throwable)"));
      }
   }

   protected void log(int priority, Object message, Throwable t)
   {
      try
      {
         m_server.invoke(m_name, "log", new Object[]{new Integer(priority), message, t}, new String[]{"int", "java.lang.Object", "java.lang.Throwable"});
      }
      catch (Exception x)
      {
         x.printStackTrace();
      }
   }
}
