/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.NotificationResult;
import javax.management.remote.TargetedNotification;

import mx4j.log.Log;
import mx4j.log.Logger;

/**
 * Base implementation of the RemoteNotificationServerHandler interface.
 *
 * @version $Revision: 1.12 $
 */
public class DefaultRemoteNotificationServerHandler implements RemoteNotificationServerHandler
{
   private static int listenerID;

   private final NotificationListener listener;
   private final Map tuples = new HashMap();
   private final NotificationBuffer buffer;
   private volatile boolean closed;

   /**
    * Creates a new remote notification server handler.
    *
    * @param environment Contains environment variables used to configure this handler
    * @see MX4JRemoteConstants#NOTIFICATION_BUFFER_CAPACITY
    * @see MX4JRemoteConstants#NOTIFICATION_PURGE_DISTANCE
    */
   public DefaultRemoteNotificationServerHandler(Map environment)
   {
      listener = new ServerListener();
      buffer = new NotificationBuffer(environment);
   }

   public Integer generateListenerID(ObjectName name, NotificationFilter filter)
   {
      synchronized (DefaultRemoteNotificationServerHandler.class)
      {
         return new Integer(++listenerID);
      }
   }

   public NotificationListener getServerNotificationListener()
   {
      return listener;
   }

   public void addNotificationListener(Integer id, NotificationTuple tuple)
   {
      if (closed) return;
      synchronized (tuples)
      {
         tuples.put(id, tuple);
      }
   }

   public NotificationTuple removeNotificationListener(Integer id)
   {
      if (closed) return null;
      synchronized (tuples)
      {
         return (NotificationTuple)tuples.remove(id);
      }
   }

   public NotificationResult fetchNotifications(long sequenceNumber, int maxNotifications, long timeout) throws IOException
   {
      if (closed) throw new IOException("RemoteNotificationServerHandler is closed");
      return buffer.getNotifications(sequenceNumber, maxNotifications, timeout);
   }

   public NotificationTuple[] close()
   {
      Logger logger = getLogger();
      closed = true;
      stopWaitingForNotifications(buffer);
      synchronized (tuples)
      {
         NotificationTuple[] result = (NotificationTuple[])tuples.values().toArray(new NotificationTuple[tuples.size()]);
         tuples.clear();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("RemoteNotificationServerHandler closed, returning: " + Arrays.asList(result));
         return result;
      }
   }

   /**
    * When a connection is closed, it may be possible that a client RMI call is waiting in
    * {@link #waitForNotifications}, so here we wake it up, letting the thread return to the
    * client and free resources on client's side.
    *
    * @param lock The object on which {@link #notifyAll} should be called
    */
   private void stopWaitingForNotifications(Object lock)
   {
      synchronized (lock)
      {
         lock.notifyAll();
      }
   }

   /**
    * Called when there are no notifications to send to the client.
    * It is guaranteed that no notification can be added before this method waits on the given lock.
    * It should wait on the given lock for the specified timeout, and return true
    * to send notifications (if no notifications arrived, an empty notification array
    * will be returned to the client), or false if no notifications should be sent to
    * the client.
    *
    * @param lock    The object on which {@link #wait} should be called
    * @param timeout The amount of time to wait (guaranteed to be strictly greater than 0)
    */
   protected boolean waitForNotifications(Object lock, long timeout)
   {
      Logger logger = getLogger();
      long start = 0;
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Waiting for notifications " + timeout + " ms");
         start = System.currentTimeMillis();
      }

      synchronized (lock)
      {
         try
         {
            lock.wait(timeout);
         }
         catch (InterruptedException x)
         {
            Thread.currentThread().interrupt();
         }
      }

      if (logger.isEnabledFor(Logger.DEBUG))
      {
         long elapsed = System.currentTimeMillis() - start;
         logger.debug("Waited for notifications " + elapsed + " ms");
      }

      return true;
   }

   /**
    * This method filters the given notification array and returns a possibly smaller array containing
    * only notifications that passed successfully the filtering.
    * Default behavior is no filtering, but subclasses may choose to change this bahavior.
    * For example, for RMI, one can assure that all notifications are truly serializable, and log those
    * that are not.
    */
   protected TargetedNotification[] filterNotifications(TargetedNotification[] notifications)
   {
      return notifications;
   }

   private void addNotification(Integer id, Notification notification)
   {
      buffer.add(new TargetedNotification(notification, id));
   }

   protected Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   private class ServerListener implements NotificationListener
   {
      public void handleNotification(Notification notification, Object handback)
      {
         Integer id = (Integer)handback;
         addNotification(id, notification);
      }
   }

   private class NotificationBuffer
   {
      private final List notifications = new LinkedList();
      private int maxCapacity;
      private int purgeDistance;
      private long firstSequence;
      private long lastSequence;
      private long lowestExpectedSequence = -1;

      private NotificationBuffer(Map environment)
      {
         if (environment != null)
         {
            try
            {
               Integer maxCapacityInteger = (Integer)environment.get(MX4JRemoteConstants.NOTIFICATION_BUFFER_CAPACITY);
               if (maxCapacityInteger != null) maxCapacity = maxCapacityInteger.intValue();
            }
            catch (Exception ignored)
            {
            }

            try
            {
               Integer purgeDistanceInteger = (Integer)environment.get(MX4JRemoteConstants.NOTIFICATION_PURGE_DISTANCE);
               if (purgeDistanceInteger != null) purgeDistance = purgeDistanceInteger.intValue();
            }
            catch (Exception ignored)
            {
            }
         }
         if (maxCapacity <= 0) maxCapacity = 1024;
         if (purgeDistance <= 0) purgeDistance = 128;
      }

      private int getSize()
      {
         synchronized (this)
         {
            return notifications.size();
         }
      }

      private void add(TargetedNotification notification)
      {
         Logger logger = getLogger();
         synchronized (this)
         {
            if (notifications.size() == maxCapacity)
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification buffer full: " + this);
               removeRange(0, 1);
            }
            notifications.add(notification);
            ++lastSequence;
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification added to buffer: " + this);
            notifyAll();
         }
      }

      private void removeRange(int start, int end)
      {
         synchronized (this)
         {
            notifications.subList(start, end).clear();
            firstSequence += end - start;
         }
      }

      private long getFirstSequenceNumber()
      {
         synchronized (this)
         {
            return firstSequence;
         }
      }

      private long getLastSequenceNumber()
      {
         synchronized (this)
         {
            return lastSequence;
         }
      }

      private NotificationResult getNotifications(long sequenceNumber, int maxNotifications, long timeout)
      {
         Logger logger = getLogger();
         synchronized (this)
         {
            NotificationResult result = null;
            int size = 0;
            if (sequenceNumber < 0)
            {
               // We loose the notifications between addNotificationListener() and fetchNotifications(), but c'est la vie.
               long sequence = getLastSequenceNumber();
               size = new Long(sequence + 1).intValue();
               result = new NotificationResult(getFirstSequenceNumber(), sequence, new TargetedNotification[0]);
               if (lowestExpectedSequence < 0) lowestExpectedSequence = sequence;
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("First fetchNotification call: " + this + ", returning " + result);
            }
            else
            {
               long firstSequence = getFirstSequenceNumber();

               int losts = 0;
               int start = new Long(sequenceNumber - firstSequence).intValue();
               // In the time between 2 fetches the buffer may have overflew, so that start < 0.
               // It simply mean that we send the first notification we have (start = 0),
               // and the client will emit a notification lost event.
               if (start < 0)
               {
                  losts = -start;
                  start = 0;
               }

               List sublist = null;
               boolean send = false;
               while (size == 0)
               {
                  int end = notifications.size();
                  if (end - start > maxNotifications) end = start + maxNotifications;

                  sublist = notifications.subList(start, end);
                  size = sublist.size();

                  if (closed || send) break;

                  if (size == 0)
                  {
                     if (timeout <= 0) break;
                     if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("No notifications to send, waiting " + timeout + " ms");

                     // We wait for notifications to arrive. Since we release the lock on the buffer
                     // other threads can modify it. To avoid ConcurrentModificationException we compute
                     // again the sublist by coming up back to the while statement
                     send = waitForNotifications(this, timeout);
                  }
               }

               TargetedNotification[] notifications = (TargetedNotification[])sublist.toArray(new TargetedNotification[size]);
               notifications = filterNotifications(notifications);
               result = new NotificationResult(firstSequence, sequenceNumber + losts + size, notifications);
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Non-first fetchNotification call: " + this + ", returning " + result);

               int purged = purgeNotifications(sequenceNumber, size);
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Purged " + purged + " notifications: " + this);
            }
            return result;
         }
      }

      private int purgeNotifications(long sequenceNumber, int size)
      {
         // Record the lowest expected sequence number sent by the client.
         // New clients will always have an initial big sequence number
         // (they're initialized with getLastSequenceNumber()), while old
         // clients can have lesser sequence numbers.
         // Here we record the lesser of these sequence numbers, that is the
         // sequence number of the oldest notification any client may ever ask.
         // This way we can purge old notifications that have already been
         // delivered to clients.

         // The worst case is when a client has a long interval between fetchNotifications()
         // calls, and another client has a short interval. The lowestExpectedSequence will
         // grow with the second client, until a purge happens, so the first client can
         // loose notifications. By tuning appropriately the purgeDistance and the interval
         // between fetchNotifications() calls, it should never happen.

         int result = 0;
         synchronized (this)
         {
            if (sequenceNumber <= lowestExpectedSequence)
            {
               long lowest = Math.min(lowestExpectedSequence, sequenceNumber);

               long firstSequence = getFirstSequenceNumber();
               if (lowest - firstSequence > purgeDistance)
               {
                  // Purge only half of the old notifications, for safety
                  int purgeSize = purgeDistance >> 1;
                  removeRange(0, purgeSize);
                  result = purgeSize;
               }

               long expected = Math.max(sequenceNumber + size, firstSequence);
               lowestExpectedSequence = expected;
            }
         }
         return result;
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer("NotificationBuffer@");
         buffer.append(Integer.toHexString(hashCode())).append("[");
         buffer.append("first=").append(getFirstSequenceNumber()).append(", ");
         buffer.append("last=").append(getLastSequenceNumber()).append(", ");
         buffer.append("size=").append(getSize()).append(", ");
         buffer.append("lowestExpected=").append(lowestExpectedSequence).append(", ");
         buffer.append("maxCapacity=").append(maxCapacity).append(", ");
         buffer.append("purgeDistance=").append(purgeDistance).append("]");
         return buffer.toString();
      }
   }
}
