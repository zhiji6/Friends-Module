/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server;

import java.security.AccessController;
import java.security.PrivilegedAction;
import javax.management.MBeanInfo;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import mx4j.MX4JSystemKeys;

/**
 * Objects of this class hold metadata information about MBeans.
 *
 * @version $Revision: 1.8 $
 * @see Factory
 */
public interface MBeanMetaData
{
   /**
    * Sets the MBean instance
    *
    * @see #getMBean
    */
   public void setMBean(Object mbean);

   /**
    * Returns the MBean instance
    *
    * @see #setMBean
    */
   public Object getMBean();

   /**
    * Sets the classloader for the MBean
    *
    * @see #getClassLoader
    */
   public void setClassLoader(ClassLoader loader);

   /**
    * Returns the classloader for the MBean
    *
    * @see #setClassLoader
    */
   public ClassLoader getClassLoader();

   /**
    * Sets the ObjectName of the MBean
    *
    * @see #getObjectName
    */
   public void setObjectName(ObjectName name);

   /**
    * Returns the ObjectName of the MBean
    *
    * @see #setObjectName
    */
   public ObjectName getObjectName();

   /**
    * Sets the MBeanInfo of the MBean
    *
    * @see #getMBeanInfo
    */
   public void setMBeanInfo(MBeanInfo info);

   /**
    * Returns the MBeanInfo of the MBean
    *
    * @see #setMBeanInfo
    */
   public MBeanInfo getMBeanInfo();

   /**
    * Sets the management interface of the standard MBean
    *
    * @see #getMBeanInterface
    */
   public void setMBeanInterface(Class management);

   /**
    * Returns the management interface of the standard MBean
    *
    * @see #setMBeanInterface
    */
   public Class getMBeanInterface();

   /**
    * Sets whether the MBean is standard
    *
    * @see #isMBeanStandard
    */
   public void setMBeanStandard(boolean value);

   /**
    * Returns whether the MBean is standard
    *
    * @see #setMBeanStandard
    */
   public boolean isMBeanStandard();

   /**
    * Sets whether the MBean is dynamic
    *
    * @see #isMBeanDynamic
    */
   public void setMBeanDynamic(boolean value);

   /**
    * Returns whether the MBean is dynamic
    *
    * @see #setMBeanDynamic
    */
   public boolean isMBeanDynamic();

   /**
    * Sets the MBeanInvoker of the standard MBean
    *
    * @see #getMBeanInvoker
    */
   public void setMBeanInvoker(MBeanInvoker invoker);

   /**
    * Returns the MBeanInvoker of the standard MBean
    *
    * @see #getMBeanInvoker
    */
   public MBeanInvoker getMBeanInvoker();

   /**
    * Returns the ObjectInstance of the MBean
    *
    * @see #getMBeanInfo
    * @see #getObjectName
    */
   public ObjectInstance getObjectInstance();

   /**
    * Factory class that creates instance of the {@link MBeanMetaData} interface.
    * The default implementation is {@link MX4JMBeanMetaData}, but it can be overridden
    * by setting the system property defined by {@link MX4JSystemKeys#MX4J_MBEAN_METADATA}.
    */
   public static class Factory
   {
      public static MBeanMetaData create()
      {
         String className = (String)AccessController.doPrivileged(new PrivilegedAction()
         {
            public Object run()
            {
               return System.getProperty(MX4JSystemKeys.MX4J_MBEAN_METADATA);
            }
         });
         if (className == null) className = "mx4j.server.MX4JMBeanMetaData";

         try
         {
            try
            {
               ClassLoader loader = MBeanMetaData.class.getClassLoader();
               if (loader == null) loader = Thread.currentThread().getContextClassLoader();
               return (MBeanMetaData)loader.loadClass(className).newInstance();
            }
            catch (ClassNotFoundException x)
            {
               // Not found with the current classloader, try the context classloader
               return (MBeanMetaData)Thread.currentThread().getContextClassLoader().loadClass(className).newInstance();
            }
         }
         catch (Exception x)
         {
            throw new Error(x.toString());
         }
      }
   }
}
