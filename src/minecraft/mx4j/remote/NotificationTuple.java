/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.5 $
 */
public class NotificationTuple
{
   private static final NotificationFilter NO_FILTER = new NotificationFilter()
   {
      public boolean isNotificationEnabled(Notification notification)
      {
         return true;
      }

      public String toString()
      {
         return "no filter";
      }
   };
   private static final Object NO_HANDBACK = new Object()
   {
      public String toString()
      {
         return "no handback";
      }
   };

   private final ObjectName observed;
   private final NotificationListener listener;
   private final NotificationFilter filter;
   private final Object handback;
   private boolean invokeFilter;

   public NotificationTuple(ObjectName observed, NotificationListener listener)
   {
      this(observed, listener, NO_FILTER, NO_HANDBACK);
   }

   public NotificationTuple(ObjectName observed, NotificationListener listener, NotificationFilter filter, Object handback)
   {
      this.observed = observed;
      this.listener = listener;
      this.filter = filter;
      this.handback = handback;
      this.invokeFilter = false;
   }

   public ObjectName getObjectName()
   {
      return observed;
   }

   public NotificationListener getNotificationListener()
   {
      return listener;
   }

   public Object getHandback()
   {
      if (handback == NO_HANDBACK) return null;
      return handback;
   }

   public NotificationFilter getNotificationFilter()
   {
      if (filter == NO_FILTER) return null;
      return filter;
   }

   public void setInvokeFilter(boolean invoke)
   {
      this.invokeFilter = invoke;
   }

   public boolean getInvokeFilter()
   {
      if (!invokeFilter) return false;
      NotificationFilter filter = getNotificationFilter();
      if (filter == null) return false;
      return true;
   }

   public boolean equals(Object obj)
   {
      if (this == obj) return true;
      if (!(obj instanceof NotificationTuple)) return false;

      final NotificationTuple other = (NotificationTuple)obj;

      if (!observed.equals(other.observed)) return false;
      if (!listener.equals(other.listener)) return false;

      // Special treatment for special filter
      if (filter == NO_FILTER) return true;
      if (other.filter == NO_FILTER) return true;

      if (filter != null ? !filter.equals(other.filter) : other.filter != null) return false;
      if (handback != null ? !handback.equals(other.handback) : other.handback != null) return false;

      return true;
   }

   public int hashCode()
   {
      int result;
      result = observed.hashCode();
      result = 29 * result + listener.hashCode();
      result = 29 * result + (filter != null ? filter.hashCode() : 0);
      result = 29 * result + (handback != null ? handback.hashCode() : 0);
      return result;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("NotificationTuple [");
      buffer.append(observed).append(", ");
      buffer.append(listener).append(", ");
      buffer.append(filter).append(", ");
      buffer.append(handback).append("]");
      return buffer.toString();
   }
}
