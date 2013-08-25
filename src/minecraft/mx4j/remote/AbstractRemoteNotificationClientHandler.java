/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * Base implementation of the RemoteNotificationClientHandler interface.
 *
 * @version $Revision: 1.7 $
 */
public abstract class AbstractRemoteNotificationClientHandler implements RemoteNotificationClientHandler
{
   private static int fetcherID;
   private static int delivererID;

   private final ConnectionNotificationEmitter emitter;
   private final HeartBeat heartbeat;
   private final Map tuples = new HashMap();
   private NotificationFetcherThread fetcherThread;
   private NotificationDelivererThread delivererThread;

   /**
    * Creates a new remote notification client-side handler.
    * It uses an emitter, an heartbeat and an environment to perform the job.
    * All 3 can be null, but the corrispondent methods must be overridden
    *
    * @param emitter     The NotificationEmitter that emits connection failures notifications
    * @param heartbeat   The heart beat is used to get the retry parameters in case of connection failure
    * @param environment Contains environment variables used to configure this handler
    * @see MX4JRemoteConstants#FETCH_NOTIFICATIONS_MAX_NUMBER
    * @see MX4JRemoteConstants#FETCH_NOTIFICATIONS_SLEEP
    * @see MX4JRemoteConstants#FETCH_NOTIFICATIONS_TIMEOUT
    * @see #sendConnectionNotificationLost
    * @see #getMaxRetries
    * @see #getRetryPeriod
    */
   protected AbstractRemoteNotificationClientHandler(ConnectionNotificationEmitter emitter, HeartBeat heartbeat, Map environment)
   {
      this.emitter = emitter;
      this.heartbeat = heartbeat;
      this.fetcherThread = new NotificationFetcherThread(environment);
      this.delivererThread = new NotificationDelivererThread(environment);
   }

   /**
    * Returns whether this client handler is fetching notifications or not.
    *
    * @see #start
    * @see #stop
    */
   public boolean isActive()
   {
      return fetcherThread.isActive();
   }

   public void start()
   {
      if (isActive()) return;
      delivererThread.start();
      fetcherThread.start();
   }

   public void stop()
   {
      if (!isActive()) return;
      fetcherThread.stop();
      delivererThread.stop();
      synchronized (tuples)
      {
         tuples.clear();
      }
   }

   private synchronized static int getFetcherID()
   {
      return ++fetcherID;
   }

   private synchronized static int getDelivererID()
   {
      return ++delivererID;
   }

   public boolean contains(NotificationTuple tuple)
   {
      synchronized (tuples)
      {
         return tuples.containsValue(tuple);
      }
   }

   public void addNotificationListener(Integer id, NotificationTuple tuple)
   {
      if (!isActive()) start();

      synchronized (tuples)
      {
         tuples.put(id, tuple);
      }

      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Adding remote NotificationListener " + tuple);
   }

   public Integer[] getNotificationListeners(NotificationTuple tuple)
   {
      synchronized (tuples)
      {
         ArrayList ids = new ArrayList();
         for (Iterator i = tuples.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getValue().equals(tuple)) ids.add(entry.getKey());
         }
         if (ids.size() > 0) return (Integer[])ids.toArray(new Integer[ids.size()]);
      }
      return null;
   }

   public Integer getNotificationListener(NotificationTuple tuple)
   {
      synchronized (tuples)
      {
         for (Iterator i = tuples.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry)i.next();
            if (entry.getValue().equals(tuple)) return (Integer)entry.getKey();
         }
      }
      return null;
   }

   public void removeNotificationListeners(Integer[] ids)
   {
      Logger logger = getLogger();
      synchronized (tuples)
      {
         for (int i = 0; i < ids.length; ++i)
         {
            Integer id = ids[i];
            NotificationTuple tuple = (NotificationTuple)tuples.remove(id);
            if (tuple != null && logger.isEnabledFor(Logger.DEBUG)) logger.debug("Removing remote NotificationListener " + tuple);
         }
      }
   }

   /**
    * Calls the server side to fetch notifications.
    */
   protected abstract NotificationResult fetchNotifications(long sequence, int maxNumber, long timeout) throws IOException;

   /**
    * Returns the period between two retries if the connection with the server side fails.
    * This implementation returns the heartbeat pulse period, but can be overridden.
    *
    * @see #getMaxRetries
    * @see #AbstractRemoteNotificationClientHandler
    */
   protected long getRetryPeriod()
   {
      return heartbeat.getPulsePeriod();
   }

   /**
    * Returns the maximum number of attempts that should be made before declaring a connection
    * failed.
    * This implementation returns the heartbeat max retries, but can be overridden.
    *
    * @see #getRetryPeriod
    * @see #AbstractRemoteNotificationClientHandler
    */
   protected int getMaxRetries()
   {
      return heartbeat.getMaxRetries();
   }

   /**
    * Sends the {@link javax.management.remote.JMXConnectionNotification#NOTIFS_LOST} notification
    * using the emitter passed to {@link AbstractRemoteNotificationClientHandler}
    */
   protected void sendConnectionNotificationLost(long number)
   {
      emitter.sendConnectionNotificationLost(number);
   }

   protected int getNotificationsCount()
   {
      return delivererThread.getNotificationsCount();
   }

   private int deliverNotifications(TargetedNotification[] notifications)
   {
      return delivererThread.addNotifications(notifications);
   }

   private void sendNotification(TargetedNotification notification)
   {
      NotificationTuple tuple = null;
      synchronized (tuples)
      {
         tuple = (NotificationTuple)tuples.get(notification.getListenerID());
      }

      // It may be possible that a notification arrived after the client already removed the listener
      if (tuple == null) return;

      Notification notif = notification.getNotification();

      Logger logger = getLogger();

      if (tuple.getInvokeFilter())
      {
         // Invoke the filter on client side
         NotificationFilter filter = tuple.getNotificationFilter();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Filtering notification " + notif + ", filter = " + filter);
         if (filter != null)
         {
            try
            {
               boolean deliver = filter.isNotificationEnabled(notif);
               if (!deliver) return;
            }
            catch (Throwable x)
            {
               logger.warn("Throwable caught from isNotificationEnabled, filter = " + filter, x);
               // And go on quietly
            }
         }
      }

      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending Notification " + notif + ", listener info is " + tuple);

      NotificationListener listener = tuple.getNotificationListener();

      try
      {
         listener.handleNotification(notif, tuple.getHandback());
      }
      catch (Throwable x)
      {
         logger.warn("Throwable caught from handleNotification, listener = " + listener, x);
         // And return quietly
      }
   }

   protected Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   private class NotificationFetcherThread implements Runnable
   {
      private long sequenceNumber;
      private volatile boolean active;
      private Thread thread;
      private long timeout;
      private int maxNumber;
      private long sleep;

      private NotificationFetcherThread(Map environment)
      {
         // Default server timeout is one minute
         timeout = 60 * 1000;
         // At most 25 notifications at time
         maxNumber = 25;
         // By default we don't sleep and we call the server again.
         sleep = 0;
         if (environment != null)
         {
            try
            {
               timeout = ((Long)environment.get(MX4JRemoteConstants.FETCH_NOTIFICATIONS_TIMEOUT)).longValue();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               maxNumber = ((Integer)environment.get(MX4JRemoteConstants.FETCH_NOTIFICATIONS_MAX_NUMBER)).intValue();
            }
            catch (Exception ignored)
            {
            }
            try
            {
               sleep = ((Integer)environment.get(MX4JRemoteConstants.FETCH_NOTIFICATIONS_SLEEP)).intValue();
            }
            catch (Exception ignored)
            {
            }
         }
      }

      private synchronized long getSequenceNumber()
      {
         return sequenceNumber;
      }

      private synchronized void setSequenceNumber(long sequenceNumber)
      {
         this.sequenceNumber = sequenceNumber;
      }

      private boolean isActive()
      {
         return active;
      }

      private synchronized void start()
      {
         active = true;
         // Initialized to a negative value for the first fetchNotification call
         sequenceNumber = -1;
         thread = new Thread(this, "Notification Fetcher #" + getFetcherID());
         thread.setDaemon(true);
         thread.start();
      }

      private synchronized void stop()
      {
         active = false;
         thread.interrupt();
      }

      public void run()
      {
         Logger logger = getLogger();
         try
         {
            while (isActive() && !thread.isInterrupted())
            {
               try
               {
                  long sequence = getSequenceNumber();
                  NotificationResult result = fetchNotifications(sequence, maxNumber, timeout);
                  if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Fetched Notifications: " + result);

                  long sleepTime = sleep;
                  if (result != null)
                  {
                     long nextSequence = result.getNextSequenceNumber();
                     TargetedNotification[] targeted = result.getTargetedNotifications();
                     int targetedLength = targeted == null ? 0 : targeted.length;
                     boolean notifsFilteredByServer = sequence >= 0 ? nextSequence - sequence != targetedLength : false;
                     boolean notifsLostByServer = sequence >= 0 && result.getEarliestSequenceNumber() > sequence;
                     if (notifsFilteredByServer)
                     {
                        // We lost some notification
                        sendConnectionNotificationLost(nextSequence - sequence - targetedLength);
                     }
                     if (notifsLostByServer)
                     {
                        // We lost some notification
                        sendConnectionNotificationLost(result.getEarliestSequenceNumber() - sequence);
                     }

                     setSequenceNumber(nextSequence);
                     int delivered = deliverNotifications(targeted);
                     if (delivered < targetedLength)
                     {
                        // We lost some notification
                        sendConnectionNotificationLost(targetedLength - delivered);
                     }

                     // If we got a maxNumber of notifications, probably the server has more to send, don't sleep
                     if (targeted != null && targeted.length == maxNumber) sleepTime = 0;
                  }

                  if (sleepTime > 0) Thread.sleep(sleepTime);
               }
               catch (IOException x)
               {
                  if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Caught IOException from fetchNotifications", x);
                  break;
               }
               catch (InterruptedException x)
               {
                  Thread.currentThread().interrupt();
                  break;
               }
               catch (Throwable x)
               {
                  if (logger.isEnabledFor(Logger.WARN)) logger.warn("Caught an unexpected exception", x);
               }
            }
         }
         finally
         {
            AbstractRemoteNotificationClientHandler.this.stop();
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug(thread.getName() + " Thread exited");
         }
      }

      /**
       * Fetches notifications from the server side in a separate thread.
       * Since it involves a remote call, IOExceptions must be handled carefully.
       * If the connection fails for any reason, the thread will be a sleep and then
       * retry for a configurable number of times.
       * If the connection is really lost, the thread will exit.
       */
      private NotificationResult fetchNotifications(long sequence, int maxNumber, long timeout) throws IOException, InterruptedException
      {
         Logger logger = getLogger();
         int retries = 0;
         while (true)
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Fetching notifications, sequence is " + sequence + ", timeout is " + timeout);
            try
            {
               return AbstractRemoteNotificationClientHandler.this.fetchNotifications(sequence, maxNumber, timeout);
            }
            catch (IOException x)
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Could not fetch notifications, sleeping " + getRetryPeriod() + " and trying " + (getMaxRetries() - retries) + " more times", x);
               Thread.sleep(getRetryPeriod());
               if (retries++ == getMaxRetries()) throw x;
            }
         }
      }
   }

   private class NotificationDelivererThread implements Runnable
   {
      private final List notificationQueue = new LinkedList();
      private int capacity;
      private volatile boolean active;
      private Thread thread;

      private NotificationDelivererThread(Map environment)
      {
         if (environment != null)
         {
            Object size = environment.get(MX4JRemoteConstants.NOTIFICATION_QUEUE_CAPACITY);
            if (size instanceof Integer)
            {
               capacity = ((Integer)size).intValue();
               if (capacity < 0) capacity = 0;
            }
         }
      }

      private int addNotifications(TargetedNotification[] notifications)
      {
         if (notifications == null || notifications.length == 0) return 0;

         List notifs = Arrays.asList(notifications);

         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Enqueuing notifications for delivery: " + notifs);

         synchronized (this)
         {
            int size = notifs.size();
            int added = size;
            if (capacity > 0)
            {
               int room = capacity - notificationQueue.size();
               if (room < size)
               {
                  added = room;
                  if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification queue is full, enqueued " + room + " notifications out of " + size + ", exceeding will be lost");
               }
               notificationQueue.addAll(notifs.subList(0, added));
            }
            else
            {
               notificationQueue.addAll(notifs);
            }
            notifyAll();
            return added;
         }
      }

      private boolean isActive()
      {
         return active;
      }

      private synchronized void start()
      {
         active = true;
         notificationQueue.clear();
         thread = new Thread(this, "Notification Deliverer #" + getDelivererID());
         thread.setDaemon(true);
         thread.start();
      }

      private synchronized void stop()
      {
         active = false;
         thread.interrupt();
      }

      public void run()
      {
         Logger logger = getLogger();
         try
         {
            while (isActive() && !thread.isInterrupted())
            {
               try
               {
                  TargetedNotification notification = null;
                  synchronized (this)
                  {
                     while (notificationQueue.isEmpty()) wait();
                     notification = (TargetedNotification)notificationQueue.remove(0);
                  }
                  sendNotification(notification);
               }
               catch (InterruptedException x)
               {
                  Thread.currentThread().interrupt();
                  break;
               }
               catch (Throwable x)
               {
                  if (logger.isEnabledFor(Logger.WARN)) logger.warn("Caught an unexpected exception", x);
               }
            }
         }
         finally
         {
            active = false;
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug(thread.getName() + " Thread exited");
         }
      }

      private int getNotificationsCount()
      {
         synchronized (this)
         {
            return notificationQueue.size();
         }
      }
   }
}
