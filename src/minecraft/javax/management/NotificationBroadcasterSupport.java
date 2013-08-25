/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * Provides an implementation of NotificationEmitter interface.
 * This can be used as the super class of an MBean that sends notifications.
 * It is not specified whether the notification dispatch model is synchronous or asynchronous.
 * That is, when a thread calls sendNotification, the NotificationListener.handleNotification
 * method of each listener may be called within that thread (a synchronous model)
 * or within some other thread (an asynchronous model).
 * Applications should not depend on notification dispatch being synchronous or being asynchronous. Thus:
 * <ul>
 * <li>Applications should not assume a synchronous model. When the sendNotification method returns,
 * it is not guaranteed that every listener's handleNotification method has been called.
 * It is not guaranteed either that a listener will see notifications in the same order as they were generated.
 * Listeners that depend on order should use the sequence number of notifications to determine their order
 * (see Notification.getSequenceNumber()).</li>
 * <li>Applications should not assume an asynchronous model.
 * If the actions performed by a listener are potentially slow, the listener should arrange for them to be performed
 * in another thread, to avoid holding up other listeners and the caller of sendNotification.</li>
 * </ul>
 *
 * @version $Revision: 1.19 $
 */
public class NotificationBroadcasterSupport implements NotificationEmitter
{
   private static final NotificationFilter NULL_FILTER = new NotificationFilter()
   {
      public boolean isNotificationEnabled(Notification notification)
      {
         return true;
      }

      public String toString()
      {
         return "null filter";
      }
   };

   private static final Object NULL_HANDBACK = new Object()
   {
      public String toString()
      {
         return "null handback";
      }
   };

   private HashMap m_listeners = new HashMap();

   public NotificationBroadcasterSupport()
   {
   }

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
   {
      Logger logger = getLogger();

      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Adding notification listener: " + listener + ", filter: " + filter + ", handback: " + handback + " to " + this);

      if (listener == null) throw new IllegalArgumentException("Notification listener cannot be null");

      // Normalize the arguments
      if (filter == null) filter = NULL_FILTER;
      if (handback == null) handback = NULL_HANDBACK;

      FilterHandbackPair pair = new FilterHandbackPair(filter, handback);

      synchronized (this)
      {
         ArrayList pairs = (ArrayList)m_listeners.get(listener);

         if (pairs == null)
         {
            // A new listener, register it
            pairs = new ArrayList();
            pairs.add(pair);
            m_listeners.put(listener, pairs);
         }
         else
         {
            // Check that the same triple (listener, filter, handback) is not already registered
            for (int i = 0; i < pairs.size(); ++i)
            {
               FilterHandbackPair other = (FilterHandbackPair)pairs.get(i);
               if (pair.filter.equals(other.filter) && pair.handback.equals(other.handback))
               {
                  // Same filter and same handback for the same listener, it's already registered
                  throw new RuntimeOperationsException(new IllegalArgumentException("Notification listener is already registered"));
               }
            }
            // Not yet registered, register.
            // Do not merge this call with the one in the if branch: like this is easier to debug
            // (I know the if-else branch from where I'm coming)
            pairs.add(pair);
         }

         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Filters - Handbacks for this listener: " + pairs);
      }

      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Notification listener added successfully to " + this);
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Removing notification listener: " + listener);

      int removed = removeNotificationListenerImpl(listener, null, null);

      if (logger.isEnabledFor(Logger.TRACE)) logger.trace(removed + " notification listener(s) removed successfully from " + this);
   }

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Removing notification listener: " + listener + ", filter: " + filter + ", handback: " + handback);

      // Normalize the arguments if necessary
      if (filter == null) filter = NULL_FILTER;
      if (handback == null) handback = NULL_HANDBACK;

      int removed = removeNotificationListenerImpl(listener, filter, handback);

      if (logger.isEnabledFor(Logger.TRACE)) logger.trace(removed + " notification listener(s) removed successfully from " + this);
   }

   private int removeNotificationListenerImpl(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      Logger logger = getLogger();
      synchronized (this)
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Listeners for " + this + " are: " + m_listeners);

         ArrayList pairs = (ArrayList)m_listeners.get(listener);

         if (pairs == null) throw new ListenerNotFoundException("NotificationListener " + listener + " not found");

         if (filter == null)
         {
            if (handback == null)
            {
               // Means I want to remove all triplets for this listener

               ArrayList removed = (ArrayList)m_listeners.remove(listener);
               return removed.size();
            }
            else
            {
               // Means I want to remove all triplets with the given handback for this listener

               int count = 0;
               for (int i = 0; i < pairs.size(); ++i)
               {
                  Object hand = ((FilterHandbackPair)pairs.get(i)).handback;
                  if (handback.equals(hand))
                  {
                     pairs.remove(i);
                     ++count;
                  }
               }
               if (count == 0) throw new ListenerNotFoundException("NotificationListener " + listener + " with handback " + handback + " not found");

               // Check if it was the last listener
               if (pairs.isEmpty()) m_listeners.remove(listener);

               return count;
            }
         }
         else
         {
            if (handback == null)
            {
               // Means I want to remove all triplets with the given filter for this listener

               int count = 0;
               for (int i = 0; i < pairs.size(); ++i)
               {
                  Object filt = ((FilterHandbackPair)pairs.get(i)).filter;
                  if (filter.equals(filt))
                  {
                     pairs.remove(i);
                     ++count;
                  }
               }
               if (count == 0) throw new ListenerNotFoundException("NotificationListener " + listener + " with filter " + filter + " not found");

               // Check if it was the last listener
               if (pairs.isEmpty()) m_listeners.remove(listener);

               return count;
            }
            else
            {
               // Means I want to remove all triplets with the given filter and handback for this listener

               int count = 0;
               for (int i = 0; i < pairs.size(); ++i)
               {
                  FilterHandbackPair pair = (FilterHandbackPair)pairs.get(i);
                  if (filter.equals(pair.filter) && handback.equals(pair.handback))
                  {
                     pairs.remove(i);
                     ++count;
                  }
               }
               if (count == 0) throw new ListenerNotFoundException("NotificationListener " + listener + " with filter " + filter + " and handback " + handback + " not found");

               // Check if it was the last listener
               if (pairs.isEmpty()) m_listeners.remove(listener);

               return count;
            }
         }
      }
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // Subclasses should override returning more informations
      return new MBeanNotificationInfo[0];
   }

   /**
    * Sends the given notification to all registered listeners
    *
    * @param notification The notification to send
    */
   public void sendNotification(Notification notification)
   {
      Logger logger = getLogger();
      boolean trace = logger.isEnabledFor(Logger.TRACE);
      boolean info = logger.isEnabledFor(Logger.INFO);

      HashMap listeners = null;

      synchronized (this)
      {
         // Clone the listeners, so we can notify without holding any lock
         // It is a shallow copy, below we will clone the pairs as well
         // I don't care if in the middle someone else adds or remove other pairs
         listeners = (HashMap)m_listeners.clone();
      }

      // Loop over all listeners
      Iterator i = listeners.keySet().iterator();

      if (i.hasNext() && trace) logger.trace("Sending notifications from " + this);

      while (i.hasNext())
      {
         NotificationListener listener = (NotificationListener)i.next();
         if (trace) logger.trace("\tListener is: " + listener);

         // Clone again the pairs for this listener.
         // I freezed the listeners with the first clone, if someone removes a pair
         // in the middle of notifications I don't care: here I clone the actual pairs
         ArrayList pairs = null;
         synchronized (this)
         {
            pairs = (ArrayList)listeners.get(listener);
            pairs = (ArrayList)pairs.clone();
         }

         if (trace) logger.trace("\tFilters - Handback for this listener: " + pairs);

         // Loop over the same listener that registered many times with different filter / handbacks
         for (int j = 0; j < pairs.size(); ++j)
         {
            FilterHandbackPair pair = (FilterHandbackPair)pairs.get(j);

            NotificationFilter filter = pair.filter;
            Object handback = pair.handback;

            // Denormalize filter and handback if necessary
            if (filter == NULL_FILTER) filter = null;
            if (handback == NULL_HANDBACK) handback = null;

            boolean enabled = false;
            try
            {
               enabled = filter == null || filter.isNotificationEnabled(notification);
            }
            catch (Throwable x)
            {
               if (info) logger.info("Throwable caught from isNotificationEnabled", x);
               // And go on
            }

            if (trace) logger.trace("\t\tFilter is: " + filter + ", enabled: " + enabled);

            if (enabled)
            {
               if (trace)
               {
                  logger.debug("\t\tHandback is: " + handback);
                  logger.debug("\t\tSending notification " + notification);
               }

               try
               {
                  handleNotification(listener, notification, handback);
               }
               catch (Throwable x)
               {
                  if (info) logger.info("Throwable caught from handleNotification", x);
                  // And go on with next listener
               }
            }
         }
      }
   }

   /**
    * This method is called by {@link #sendNotification} for each listener in order to send the notification to that listener.
    * It can be overridden in subclasses to change the behaviour of notification delivery,
    * for instance to deliver the notification in a separate thread.
    * It is not guaranteed that this method is called by the same thread as the one that called sendNotification.
    * The default implementation of this method is equivalent to
    * <code>listener.handleNotification(notif, handback);</code>
    *
    * @param listener     - the listener to which the notification is being delivered.
    * @param notification - the notification being delivered to the listener.
    * @param handback     - the handback object that was supplied when the listener was added.
    * @since JMX 1.2
    */
   protected void handleNotification(NotificationListener listener, Notification notification, Object handback)
   {
      listener.handleNotification(notification, handback);
   }

   private static class FilterHandbackPair
   {
      private NotificationFilter filter;
      private Object handback;

      private FilterHandbackPair(NotificationFilter filter, Object handback)
      {
         this.filter = filter;
         this.handback = handback;
      }
   }
}
