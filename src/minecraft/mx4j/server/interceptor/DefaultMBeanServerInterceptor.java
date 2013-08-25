/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import java.util.List;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ReflectionException;

import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.server.MBeanMetaData;

/**
 * Base class for MBeanServer --&gt; MBean interceptors.
 *
 * @version $Revision: 1.11 $
 */
public abstract class DefaultMBeanServerInterceptor implements MBeanServerInterceptor, DefaultMBeanServerInterceptorMBean
{
   private boolean enabled = true;
   private String logCategory;
   private List chain;

   protected DefaultMBeanServerInterceptor()
   {
      // It's amazing how setting up here this string dramatically reduces the times to get the Logger instance
      logCategory = getClass().getName() + "." + getType();
   }

   /**
    * Returns whether this interceptor is enabled
    *
    * @see #setEnabled
    */
   public boolean isEnabled()
   {
      return enabled;
   }

   /**
    * Enables or disables this interceptor
    *
    * @see #isEnabled
    */
   public void setEnabled(boolean enabled)
   {
      this.enabled = enabled;
   }

   /**
    * Returns the type of this interceptor
    */
   public abstract String getType();

   protected synchronized MBeanServerInterceptor getNext()
   {
      int index = chain.indexOf(this);
      MBeanServerInterceptor next = (MBeanServerInterceptor)chain.get(index + 1);
      next.setChain(chain);
      return next;
   }

   public synchronized void setChain(List chain)
   {
      this.chain = chain;
   }

   protected Logger getLogger()
   {
      return Log.getLogger(logCategory);
   }

   public void addNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback)
   {
      getNext().addNotificationListener(metadata, listener, filter, handback);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener) throws ListenerNotFoundException
   {
      getNext().removeNotificationListener(metadata, listener);
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      getNext().removeNotificationListener(metadata, listener, filter, handback);
   }

   public void instantiate(MBeanMetaData metadata, String className, String[] params, Object[] args) throws ReflectionException, MBeanException
   {
      getNext().instantiate(metadata, className, params, args);
   }

   public void registration(MBeanMetaData metadata, int operation) throws MBeanRegistrationException
   {
      getNext().registration(metadata, operation);
   }

   public MBeanInfo getMBeanInfo(MBeanMetaData metadata)
   {
      return getNext().getMBeanInfo(metadata);
   }

   public Object invoke(MBeanMetaData metadata, String method, String[] params, Object[] args) throws MBeanException, ReflectionException
   {
      return getNext().invoke(metadata, method, params, args);
   }

   public AttributeList getAttributes(MBeanMetaData metadata, String[] attributes)
   {
      return getNext().getAttributes(metadata, attributes);
   }

   public AttributeList setAttributes(MBeanMetaData metadata, AttributeList attributes)
   {
      return getNext().setAttributes(metadata, attributes);
   }

   public Object getAttribute(MBeanMetaData metadata, String attribute) throws MBeanException, AttributeNotFoundException, ReflectionException
   {
      return getNext().getAttribute(metadata, attribute);
   }

   public void setAttribute(MBeanMetaData metadata, Attribute attribute) throws MBeanException, AttributeNotFoundException, InvalidAttributeValueException, ReflectionException
   {
      getNext().setAttribute(metadata, attribute);
   }
}
