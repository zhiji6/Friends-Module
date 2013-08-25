/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InvalidAttributeValueException;
import javax.management.JMRuntimeException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;

import mx4j.ImplementationException;
import mx4j.log.Logger;
import mx4j.server.MBeanMetaData;
import mx4j.util.Utils;

/**
 * The last MBeanServer --$gt; MBean interceptor in the chain.
 * It calls the MBean instance; if the MBean is a dynamic MBean, the call is direct, otherwise the call is delegated
 * to an {@link mx4j.server.MBeanInvoker MBeanInvoker}.
 *
 * @version $Revision: 1.23 $
 */
public class InvokerMBeanServerInterceptor extends DefaultMBeanServerInterceptor implements InvokerMBeanServerInterceptorMBean
{
   private MBeanServer outerServer;

   /**
    * Instantiates a new interceptor instance.
    *
    * @param outerServer the {@link MBeanServer} instance that is passed to
    *                    {@link MBeanRegistration#preRegister(MBeanServer, ObjectName)}.
    */
   public InvokerMBeanServerInterceptor(MBeanServer outerServer)
   {
      this.outerServer = outerServer;
   }

   /**
    * Returns the type of this interceptor
    */
   public String getType()
   {
      return "invoker";
   }

   /**
    * This interceptor is always enabled
    */
   public boolean isEnabled()
   {
      return true;
   }

   public void addNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback)
   {
      ((NotificationBroadcaster)metadata.getMBean()).addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener) throws ListenerNotFoundException
   {
      ((NotificationBroadcaster)metadata.getMBean()).removeNotificationListener(listener);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback)
           throws ListenerNotFoundException
   {
      ((NotificationEmitter)metadata.getMBean()).removeNotificationListener(listener, filter, handback);
   }

   public void instantiate(MBeanMetaData metadata, String className, String[] params, Object[] args) throws ReflectionException, MBeanException
   {
      try
      {
         ClassLoader loader = metadata.getClassLoader();
         if (loader == null) loader = Thread.currentThread().getContextClassLoader();
         Class cls = loader.loadClass(className);

         Class[] signature = Utils.loadClasses(loader, params);

         Constructor ctor = cls.getConstructor(signature);

         metadata.setMBean(ctor.newInstance(args));
      }
      catch (ClassNotFoundException x)
      {
         throw new ReflectionException(x);
      }
      catch (NoSuchMethodException x)
      {
         throw new ReflectionException(x);
      }
      catch (InstantiationException x)
      {
         throw new ReflectionException(x);
      }
      catch (IllegalAccessException x)
      {
         throw new ReflectionException(x);
      }
      catch (IllegalArgumentException x)
      {
         throw new ReflectionException(x);
      }
      catch (InvocationTargetException x)
      {
         Throwable t = x.getTargetException();
         if (t instanceof Error)
         {
            throw new RuntimeErrorException((Error)t);
         }
         else if (t instanceof RuntimeException)
         {
            throw new RuntimeMBeanException((RuntimeException)t);
         }
         else
         {
            throw new MBeanException((Exception)t);
         }
      }
   }

   public void registration(MBeanMetaData metadata, int operation) throws MBeanRegistrationException
   {
      Object mbean = metadata.getMBean();
      if (!(mbean instanceof MBeanRegistration)) return;

      MBeanRegistration registrable = (MBeanRegistration)mbean;

      try
      {
         switch (operation)
         {
            case PRE_REGISTER:
               ObjectName objName = registrable.preRegister(outerServer, metadata.getObjectName());
               metadata.setObjectName(objName);
               break;
            case POST_REGISTER_TRUE:
               registrable.postRegister(Boolean.TRUE);
               break;
            case POST_REGISTER_FALSE:
               registrable.postRegister(Boolean.FALSE);
               break;
            case PRE_DEREGISTER:
               registrable.preDeregister();
               break;
            case POST_DEREGISTER:
               registrable.postDeregister();
               break;
            default:
               throw new ImplementationException();
         }
      }
      catch (RuntimeException x)
      {
         throw new RuntimeMBeanException(x);
      }
      catch (Exception x)
      {
         if (x instanceof MBeanRegistrationException)
         {
            throw (MBeanRegistrationException)x;
         }
         throw new MBeanRegistrationException(x);
      }
      catch (Error x)
      {
         throw new RuntimeErrorException(x);
      }
   }

   public MBeanInfo getMBeanInfo(MBeanMetaData metadata)
   {
      if (metadata.isMBeanDynamic())
      {
         // From JMX 1.1 the MBeanInfo may be dynamically changed at every time, let's refresh it
         MBeanInfo info = null;
         try
         {
            info = ((DynamicMBean)metadata.getMBean()).getMBeanInfo();
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         if (info == null) return null;
         metadata.setMBeanInfo(info);
      }

      return (MBeanInfo)metadata.getMBeanInfo().clone();
   }

   public Object invoke(MBeanMetaData metadata, String method, String[] params, Object[] args) throws MBeanException, ReflectionException
   {
      if (metadata.isMBeanDynamic())
      {
         try
         {
            return ((DynamicMBean)metadata.getMBean()).invoke(method, args, params);
         }
         catch (JMRuntimeException x)
         {
            throw x;
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         catch (Error x)
         {
            throw new RuntimeErrorException(x);
         }
      }
      else
      {
         return metadata.getMBeanInvoker().invoke(metadata, method, params, args);
      }
   }

   public Object getAttribute(MBeanMetaData metadata, String attribute) throws MBeanException, AttributeNotFoundException, ReflectionException
   {
      if (metadata.isMBeanDynamic())
      {
         try
         {
            return ((DynamicMBean)metadata.getMBean()).getAttribute(attribute);
         }
         catch (JMRuntimeException x)
         {
            throw x;
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         catch (Error x)
         {
            throw new RuntimeErrorException(x);
         }
      }
      else
      {
         return metadata.getMBeanInvoker().getAttribute(metadata, attribute);
      }
   }

   public void setAttribute(MBeanMetaData metadata, Attribute attribute) throws MBeanException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException
   {
      if (metadata.isMBeanDynamic())
      {
         try
         {
            ((DynamicMBean)metadata.getMBean()).setAttribute(attribute);
         }
         catch (JMRuntimeException x)
         {
            throw x;
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         catch (Error x)
         {
            throw new RuntimeErrorException(x);
         }
      }
      else
      {
         metadata.getMBeanInvoker().setAttribute(metadata, attribute);
      }
   }

   public AttributeList getAttributes(MBeanMetaData metadata, String[] attributes)
   {
      if (metadata.isMBeanDynamic())
      {
         try
         {
            return ((DynamicMBean)metadata.getMBean()).getAttributes(attributes);
         }
         catch (JMRuntimeException x)
         {
            throw x;
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         catch (Error x)
         {
            throw new RuntimeErrorException(x);
         }
      }
      else
      {
         AttributeList list = new AttributeList();
         for (int i = 0; i < attributes.length; ++i)
         {
            String name = attributes[i];
            try
            {
               Object value = getAttribute(metadata, name);
               Attribute attr = new Attribute(name, value);
               list.add(attr);
            }
            catch (Exception ignored)
            {
               Logger logger = getLogger();
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Exception caught from getAttributes(), ignoring attribute " + name);
            }
         }
         return list;
      }
   }

   public AttributeList setAttributes(MBeanMetaData metadata, AttributeList attributes)
   {
      if (metadata.isMBeanDynamic())
      {
         try
         {
            return ((DynamicMBean)metadata.getMBean()).setAttributes(attributes);
         }
         catch (JMRuntimeException x)
         {
            throw x;
         }
         catch (RuntimeException x)
         {
            throw new RuntimeMBeanException(x);
         }
         catch (Error x)
         {
            throw new RuntimeErrorException(x);
         }
      }
      else
      {
         AttributeList list = new AttributeList();
         for (int i = 0; i < attributes.size(); ++i)
         {
            Attribute attr = (Attribute)attributes.get(i);
            try
            {
               setAttribute(metadata, attr);
               list.add(attr);
            }
            catch (Exception ignored)
            {
               Logger logger = getLogger();
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Exception caught from setAttributes(), ignoring attribute " + attr, ignored);
            }
         }
         return list;
      }
   }
}
