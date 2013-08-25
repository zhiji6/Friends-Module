/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.timer;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.timer.TimeQueue;
import mx4j.timer.TimerTask;

/**
 * @version $Revision: 1.19 $
 */
public class Timer extends NotificationBroadcasterSupport implements TimerMBean, MBeanRegistration
{
   public static final long ONE_SECOND = 1000;
   public static final long ONE_MINUTE = 60 * ONE_SECOND;
   public static final long ONE_HOUR = 60 * ONE_MINUTE;
   public static final long ONE_DAY = 24 * ONE_HOUR;
   public static final long ONE_WEEK = 7 * ONE_DAY;

   private TimeQueue queue = new TimeQueue();
   private boolean isActive;
   private int notificationID;
   private HashMap tasks = new HashMap();
   private boolean sendPastNotification;
   private ObjectName objectName;

   private Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      Logger logger = getLogger();
      objectName = name;
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " preRegistered successfully");
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      Logger logger = getLogger();
      boolean done = registrationDone.booleanValue();
      if (!done)
      {
         logger.warn("Timer service " + objectName + " was not registered");
      }
      else
      {
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " postRegistered successfully.");
      }
   }

   public void preDeregister() throws Exception
   {
      Logger logger = getLogger();
      stop();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " preDeregistered successfully");
   }

   public void postDeregister()
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " postDeregistered successfully");
   }

   public void start()
   {
      if (!isActive())
      {
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Starting Timer service " + objectName);

         queue.clear();
         queue.start();

         ArrayList tasks = updateTasks();
         scheduleTasks(tasks);

         isActive = true;

         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " started successfully");
      }
   }

   public void stop()
   {
      if (isActive())
      {
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Stopping Timer service " + objectName);

         queue.stop();
         queue.clear();

         isActive = false;

         if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Timer service " + objectName + " stopped successfully");
      }
   }

   public boolean isActive()
   {
      return isActive;
   }

   public Integer addNotification(String type, String message, Object userData, Date date) throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, 0, 0, false);
   }

   public Integer addNotification(String type, String message, Object userData, Date date, long period) throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, period, 0, false);
   }

   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurrences) throws IllegalArgumentException
   {
      return addNotification(type, message, userData, date, period, occurrences, false);
   }

   public Integer addNotification(String type, String message, Object userData, Date date, long period, long occurrences, boolean fixedRate) throws IllegalArgumentException
   {
      if (date == null) throw new IllegalArgumentException("Notification date cannot be null");
      if (period < 0) throw new IllegalArgumentException("Period cannot be negative");
      if (occurrences < 0) throw new IllegalArgumentException("Occurrences cannot be negative");

      long now = System.currentTimeMillis();

      if (isActive())
      {
         // Check for the validity of the notification times, as the Timer is active

         // Notification in the past, assume it's now
         if (date.getTime() < now) date = new Date(now);

         // Periodic limited, the last notification is in the past
         if (period > 0 && occurrences > 0)
         {
            long lastTime = date.getTime() + (occurrences - 1) * period;
            if (lastTime < now) throw new IllegalArgumentException("Last date for periodic notification is before current date");
         }
      }

      // Anyway, register the notification, no matter if the Timer is active
      Integer id = addNotificationImpl(type, message, userData, date, period, occurrences, fixedRate);

      // If the Timer is active, schedule the notification
      if (isActive())
      {
         TimerTask task = getTask(id);
         updateTask(task, now);
         if (!task.isFinished())
         {
            queue.schedule(task);
         }
      }
      return id;
   }

   private Integer addNotificationImpl(String type, String message, Object userData, Date date, long period, long occurrences, boolean fixedRate)
   {
      Logger logger = getLogger();

      Integer id = createNotificationID();

      TimerNotification notification = new TimerNotification(type, this, 0, System.currentTimeMillis(), message, id);
      notification.setUserData(userData);
      if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Adding timer notification: " + notification + " on Timer service " + objectName);

      TimerTask task = createTimerTask(notification, date, period, occurrences, fixedRate);

      synchronized (this)
      {
         tasks.put(id, task);
      }

      return id;
   }

   private TimerTask createTimerTask(TimerNotification notification, Date date, long period, long occurrences, boolean fixedRate)
   {
      return new TimerTask(notification, date, period, occurrences, fixedRate)
      {
         public void run()
         {
            // Send the notification
            TimerNotification notification = getNotification();
            TimerNotification toSend;
            synchronized(notification)
            {
               toSend = new TimerNotification(notification.getType(), notification.getSource(), notification.getSequenceNumber(), notification.getTimeStamp(), notification.getMessage(), notification.getNotificationID());
               toSend.setUserData(notification.getUserData());
               notification.setSequenceNumber(notification.getSequenceNumber() + 1);
            }
            sendNotification(toSend);
         }
      };
   }

   private ArrayList updateTasks()
   {
      ArrayList list = new ArrayList();
      boolean sendPast = getSendPastNotifications();
      long now = System.currentTimeMillis();
      synchronized (this)
      {
         for (Iterator i = tasks.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = getNextNonFinishedTaskEntry(i);
            if (entry == null) break;

            TimerTask task = (TimerTask)entry.getValue();

            if (!sendPast)
            {
               updateTask(task, now);
               if (task.isFinished()) continue;
            }
            list.add(task);
         }
         return list;
      }
   }

   private void updateTask(TimerTask task, long now)
   {
      long time = task.getNextExecutionTime();

      while (time < now && !task.isFinished())
      {
         if (task.isPeriodic())
         {
            task.setNextExecutionTime(time + task.getPeriod());
            time = task.getNextExecutionTime();
         }
         else
         {
            task.setFinished(true);
         }
      }
   }

   private void scheduleTasks(ArrayList tasks)
   {
      synchronized (this)
      {
         for (int i = 0; i < tasks.size(); ++i)
         {
            TimerTask task = (TimerTask)tasks.get(i);
            queue.schedule(task);
         }
      }
   }

   public void removeNotification(Integer id) throws InstanceNotFoundException
   {
      Logger logger = getLogger();

      synchronized (this)
      {
         TimerTask t = getTask(id);
         if (t == null) throw new InstanceNotFoundException("Cannot find notification to remove with id: " + id);
         queue.unschedule(t);
         tasks.remove(id);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification " + t.getNotification() + " removed successfully from Timer service " + objectName);
      }
   }

   public void removeNotifications(String type) throws InstanceNotFoundException
   {
      Logger logger = getLogger();

      boolean found = false;
      synchronized (this)
      {
         for (Iterator i = tasks.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = getNextNonFinishedTaskEntry(i);
            if (entry == null) break;

            TimerTask t = (TimerTask)entry.getValue();
            TimerNotification n = t.getNotification();
            if (n.getType().equals(type))
            {
               queue.unschedule(t);
               i.remove();
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Notification " + n + " removed successfully from Timer service " + objectName);
               found = true;
            }
         }
      }

      if (!found) throw new InstanceNotFoundException("Cannot find timer notification to remove with type: " + type + " from Timer service " + objectName);
   }

   public void removeAllNotifications()
   {
      synchronized (this)
      {
         queue.clear();
         tasks.clear();
         notificationID = 0;
      }
   }

   public Vector getAllNotificationIDs()
   {
      Vector vector = new Vector();
      synchronized (this)
      {
         for (Iterator i = tasks.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = getNextNonFinishedTaskEntry(i);
            if (entry == null) break;
            vector.add(entry.getKey());
         }
      }
      return vector;
   }

   public Vector getNotificationIDs(String type)
   {
      Vector vector = new Vector();
      synchronized (this)
      {
         for (Iterator i = tasks.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = getNextNonFinishedTaskEntry(i);
            if (entry == null) break;
            TimerTask t = (TimerTask)entry.getValue();
            TimerNotification n = t.getNotification();
            if (n.getType().equals(type))
            {
               vector.add(entry.getKey());
            }
         }
      }
      return vector;
   }

   public boolean getSendPastNotifications()
   {
      return sendPastNotification;
   }

   public void setSendPastNotifications(boolean value)
   {
      sendPastNotification = value;
   }

   public int getNbNotifications()
   {
      int count = 0;
      synchronized (this)
      {
         for (Iterator i = tasks.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = getNextNonFinishedTaskEntry(i);
            if (entry == null) break;
            ++count;
         }
         return count;
      }
   }

   public boolean isEmpty()
   {
      synchronized (this)
      {
         return getNbNotifications() == 0;
      }
   }

   public String getNotificationType(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : t.getNotification().getType();
      }
   }

   public String getNotificationMessage(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : t.getNotification().getMessage();
      }
   }

   public Object getNotificationUserData(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : t.getNotification().getUserData();
      }
   }

   public Date getDate(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : new Date(t.getDate());
      }
   }

   public Long getPeriod(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : new Long(t.getPeriod());
      }
   }

   public Long getNbOccurences(Integer id)
   {
      synchronized (this)
      {
         TimerTask t = getTask(id);
         return t == null ? null : new Long(t.getOccurrences());
      }
   }

   private Integer createNotificationID()
   {
      synchronized (this)
      {
         return new Integer(++notificationID);
      }
   }

   private TimerTask getTask(Integer id)
   {
      Logger logger = getLogger();

      synchronized (this)
      {
         TimerTask t = (TimerTask)tasks.get(id);

         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Retrieving task with id " + id + ": " + t);

         if (t != null && t.isFinished())
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Task with id " + id + " is expired, removing it");

            tasks.remove(id);
            t = null;
         }
         return t;
      }
   }

   private Map.Entry getNextNonFinishedTaskEntry(Iterator i)
   {
      Logger logger = getLogger();

      synchronized (this)
      {
         if (i.hasNext())
         {
            Map.Entry entry = (Map.Entry)i.next();
            TimerTask t = (TimerTask)entry.getValue();
            if (t.isFinished())
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Found an expired notification, removing it: " + t);
               i.remove();
               return getNextNonFinishedTaskEntry(i);
            }
            return entry;
         }
         return null;
      }
   }

   public void sendNotification(Notification n)
   {
      Logger logger = getLogger();
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Start sending notifications from Timer service " + objectName);
      super.sendNotification(n);
      if (logger.isEnabledFor(Logger.TRACE)) logger.trace("Finished sending notifications from Timer service " + objectName);
   }

   public Boolean getFixedRate(Integer id)
   {
      return new Boolean(getTask(id).getFixedRate());
   }
}
