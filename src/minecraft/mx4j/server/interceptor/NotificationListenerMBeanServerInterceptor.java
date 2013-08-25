/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.server.interceptor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

import mx4j.server.MBeanMetaData;

/**
 * Interceptor that takes care of replacing the source of Notifications to the
 * ObjectName of the NotificationBroadcaster that emitted it.
 *
 * @version $Revision: 1.14 $
 */
public class NotificationListenerMBeanServerInterceptor extends DefaultMBeanServerInterceptor
{
   private final Map wrappers = new HashMap();
   private final Map objectNames = new HashMap();

   public String getType()
   {
      return "notificationlistener";
   }

   public void addNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback)
   {
      if (isEnabled())
      {
         ListenerWrapper wrapper = null;
         synchronized (wrappers)
         {
            ListenerWrapperKey key = new ListenerWrapperKey(listener, metadata.getObjectName());
            wrapper = (ListenerWrapper)wrappers.get(key);
            if (wrapper == null)
            {
               wrapper = new ListenerWrapper(listener, metadata.getObjectName());
               wrappers.put(key, wrapper);
               wrapper.increaseReferenceCount();
            }
            else
            {
               // In case the listener is added twice to the same MBean,
               // for example with different handbacks or filters
               wrapper.increaseReferenceCount();
            }

            Set keys = (Set)objectNames.get(metadata.getObjectName());
            if (keys == null)
            {
               // The MBean has no listeners
               keys = new HashSet();
               objectNames.put(metadata.getObjectName(), keys);
               keys.add(key);
            }
            else
            {
               // The MBean has other listeners; in case the same listener
               // is added twice to the same MBean, for example with different
               // handback or filters, the Set semantic will retain the key only once.
               keys.add(key);
            }
         }

         super.addNotificationListener(metadata, wrapper, filter, handback);
      }
      else
      {
         super.addNotificationListener(metadata, listener, filter, handback);
      }
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener) throws ListenerNotFoundException
   {
      if (isEnabled())
      {
         ListenerWrapper wrapper = null;
         synchronized (wrappers)
         {
            ListenerWrapperKey key = new ListenerWrapperKey(listener, metadata.getObjectName());
            wrapper = (ListenerWrapper)wrappers.remove(key);
            if (wrapper == null) throw new ListenerNotFoundException("Could not find listener " + listener);
            wrapper.resetReferenceCount();

            Set keys = (Set)objectNames.get(metadata.getObjectName());
            keys.remove(key);
            if (keys.isEmpty()) objectNames.remove(metadata.getObjectName());
         }
         super.removeNotificationListener(metadata, wrapper);
      }
      else
      {
         super.removeNotificationListener(metadata, listener);
      }
   }

   public void removeNotificationListener(MBeanMetaData metadata, NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      if (isEnabled())
      {
         ListenerWrapper wrapper = null;
         synchronized (wrappers)
         {
            ListenerWrapperKey key = new ListenerWrapperKey(listener, metadata.getObjectName());
            wrapper = (ListenerWrapper)wrappers.get(key);
            if (wrapper == null) throw new ListenerNotFoundException("Could not find listener " + listener);
            wrapper.decreaseReferenceCount();
            if (wrapper.getReferenceCount() == 0)
            {
               wrappers.remove(key);
               Set keys = (Set)objectNames.get(metadata.getObjectName());
               keys.remove(key);
               if (keys.isEmpty()) objectNames.remove(metadata.getObjectName());
            }
         }
         super.removeNotificationListener(metadata, wrapper, filter, handback);
      }
      else
      {
         super.removeNotificationListener(metadata, listener, filter, handback);
      }
   }

   public void registration(MBeanMetaData metadata, int operation) throws MBeanRegistrationException
   {
      if (isEnabled())
      {
         if (operation == POST_DEREGISTER)
         {
            // We must clean up in case the MBean is unregistered
            // and the listeners are not removed
            synchronized (wrappers)
            {
               Set keys = (Set)objectNames.remove(metadata.getObjectName());
               if (keys != null)
               {
                  for (Iterator iterator = keys.iterator(); iterator.hasNext();)
                  {
                     ListenerWrapperKey key = (ListenerWrapperKey)iterator.next();
                     ListenerWrapper wrapper = (ListenerWrapper)wrappers.remove(key);
                     wrapper.resetReferenceCount();
                  }
               }
            }
         }
         super.registration(metadata, operation);
      }
      else
      {
         super.registration(metadata, operation);
      }
   }

   public Map getNotificationListenerWrappers()
   {
      return wrappers;
   }

   public int getNotificationListenerWrapperReferenceCount(Object wrapper)
   {
      ListenerWrapper listenerWrapper = (ListenerWrapper)wrapper;
      return listenerWrapper.getReferenceCount();
   }

   public Map getObjectNames()
   {
      return objectNames;
   }

   private static class ListenerWrapper implements NotificationListener
   {
      private final NotificationListener listener;
      private final ObjectName objectName;
      private int referenceCount;

      private ListenerWrapper(NotificationListener listener, ObjectName name)
      {
         this.listener = listener;
         this.objectName = name;
      }

      public void handleNotification(Notification notification, Object handback)
      {
         // The JMX spec does not specify how to change the source to be the ObjectName
         // of the broadcaster. If we serialize the calls to the listeners, then it's
         // possible to change the source and restore it back to the old value before
         // calling the next listener; but if we want to support concurrent calls
         // to the listeners, this is not possible. Here I chose to support concurrent
         // calls so I change the value once and I never restore it.
         Object src = notification.getSource();
         if (!(src instanceof ObjectName))
         {
            // Change the source to be the ObjectName of the notification broadcaster
            // if we are not already an ObjectName (compliant with RI behaviour)
            notification.setSource(objectName);
         }

         // Notify the real listener
         NotificationListener listener = getTargetListener();
         listener.handleNotification(notification, handback);
      }

      private NotificationListener getTargetListener()
      {
         return listener;
      }

      private int increaseReferenceCount()
      {
         return ++referenceCount;
      }

      private int decreaseReferenceCount()
      {
         return --referenceCount;
      }

      public void resetReferenceCount()
      {
         referenceCount = 0;
      }

      private int getReferenceCount()
      {
         return referenceCount;
      }

      public int hashCode()
      {
         return getTargetListener().hashCode();
      }

      public boolean equals(Object obj)
      {
         if (this == obj) return true;
         if (obj == null || getClass() != obj.getClass()) return false;
         final ListenerWrapper wrapper = (ListenerWrapper)obj;
         return getTargetListener().equals(wrapper.getTargetListener());
      }

      public String toString()
      {
         return getTargetListener().toString();
      }
   }

   private static class ListenerWrapperKey
   {
      private final NotificationListener listener;
      private final ObjectName objectName;

      private ListenerWrapperKey(NotificationListener listener, ObjectName objectName)
      {
         this.listener = listener;
         this.objectName = objectName;
      }

      public ObjectName getObjectName()
      {
         return objectName;
      }

      public boolean equals(Object obj)
      {
         if (this == obj) return true;
         if (obj == null || getClass() != obj.getClass()) return false;
         final ListenerWrapperKey that = (ListenerWrapperKey)obj;
         if (!listener.equals(that.listener)) return false;
         return objectName.equals(that.objectName);
      }

      public int hashCode()
      {
         int result = listener.hashCode();
         result = 29 * result + objectName.hashCode();
         return result;
      }
   }
}
