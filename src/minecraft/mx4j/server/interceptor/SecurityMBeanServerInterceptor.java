/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import java.security.AccessControlException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanPermission;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanTrustPermission;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import mx4j.server.MBeanMetaData;

/**
 * Interceptor that takes care of performing security checks (in case the SecurityManager is installed) for
 * MBeanServer to MBean calls.
 *
 * @version $Revision: 1.14 $
 */
public class SecurityMBeanServerInterceptor extends DefaultMBeanServerInterceptor implements SecurityMBeanServerInterceptorMBean
{
   public String getType()
   {
      return "security";
   }

   public boolean isEnabled()
   {
      return true;
   }

   public void addNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback)
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "addNotificationListener");
      super.addNotificationListener(metadata, listener, filter, handback);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener) throws ListenerNotFoundException
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "removeNotificationListener");
      super.removeNotificationListener(metadata, listener);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "removeNotificationListener");
      super.removeNotificationListener(metadata, listener, filter, handback);
   }

   public void instantiate(MBeanMetaData metadata, String className, String[] params, Object[] args) throws ReflectionException, MBeanException
   {
      checkPermission(className, null, metadata.getObjectName(), "instantiate");
      super.instantiate(metadata, className, params, args);
   }

   public MBeanInfo getMBeanInfo(MBeanMetaData metadata)
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "getMBeanInfo");
      return super.getMBeanInfo(metadata);
   }

   public Object invoke(MBeanMetaData metadata, String method, String[] params, Object[] args) throws MBeanException, ReflectionException
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), method, metadata.getObjectName(), "invoke");
      return super.invoke(metadata, method, params, args);
   }

   public AttributeList getAttributes(MBeanMetaData metadata, String[] attributes)
   {
      Object[] secured = filterAttributes(metadata.getMBeanInfo().getClassName(), metadata.getObjectName(), attributes, true);
      String[] array = new String[secured.length];
      for (int i = 0; i < array.length; ++i) array[i] = (String)secured[i];
      return super.getAttributes(metadata, array);
   }

   public AttributeList setAttributes(MBeanMetaData metadata, AttributeList attributes)
   {
      Object[] secured = filterAttributes(metadata.getMBeanInfo().getClassName(), metadata.getObjectName(), attributes.toArray(), false);
      AttributeList list = new AttributeList();
      for (int i = 0; i < secured.length; ++i) list.add(secured[i]);
      return super.setAttributes(metadata, list);
   }

   public Object getAttribute(MBeanMetaData metadata, String attribute) throws MBeanException, AttributeNotFoundException, ReflectionException
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), attribute, metadata.getObjectName(), "getAttribute");
      return super.getAttribute(metadata, attribute);
   }

   public void setAttribute(MBeanMetaData metadata, Attribute attribute) throws MBeanException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException
   {
      checkPermission(metadata.getMBeanInfo().getClassName(), attribute.getName(), metadata.getObjectName(), "setAttribute");
      super.setAttribute(metadata, attribute);
   }

   public void registration(MBeanMetaData metadata, int operation) throws MBeanRegistrationException
   {
      switch (operation)
      {
         case PRE_REGISTER:
            checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "registerMBean");
            checkTrustRegistration(metadata.getMBean().getClass());
            break;
         case POST_REGISTER_TRUE:
            // The MBean can implement MBeanRegistration and change the ObjectName
            checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "registerMBean");
            break;
         case PRE_DEREGISTER:
            checkPermission(metadata.getMBeanInfo().getClassName(), null, metadata.getObjectName(), "unregisterMBean");
            break;
         default:
            break;
      }
      super.registration(metadata, operation);
   }

   private void checkPermission(String className, String methodName, ObjectName objectname, String action)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         sm.checkPermission(new MBeanPermission(className, methodName, objectname, action));
      }
   }

   private void checkTrustRegistration(final Class cls)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm != null)
      {
         ProtectionDomain domain = (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction()
         {
            public Object run()
            {
               return cls.getProtectionDomain();
            }
         });

         MBeanTrustPermission permission = new MBeanTrustPermission("register");
         if (!domain.implies(permission))
         {
            throw new AccessControlException("Access denied " + permission + ": MBean class " + cls.getName() + " is not trusted for registration");
         }
      }
   }

   private Object[] filterAttributes(String className, ObjectName objectName, Object[] attributes, boolean isGet)
   {
      SecurityManager sm = System.getSecurityManager();
      if (sm == null) return attributes;

      ArrayList list = new ArrayList();

      for (int i = 0; i < attributes.length; ++i)
      {
         Object attribute = attributes[i];
         String name = isGet ? (String)attribute : ((Attribute)attribute).getName();

         try
         {
            checkPermission(className, name, objectName, isGet ? "getAttribute" : "setAttribute");
            list.add(attribute);
         }
         catch (SecurityException ignore)
         {
            // This is ok.  We just don't add this attribute to the list
         }
      }

      return list.toArray();
   }
}
