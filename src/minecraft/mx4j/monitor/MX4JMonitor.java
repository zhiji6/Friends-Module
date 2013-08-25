/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.StandardMBean;
import javax.management.monitor.MonitorNotification;

import mx4j.log.Log;
import mx4j.log.Logger;
import mx4j.timer.TimeQueue;
import mx4j.timer.TimeTask;

/**
 * The class that implements the Monitor behavior of the JMX specification.
 * IMPLEMENTATION NOTE:
 * There is one single thread that handles monitoring, for all monitor objects.
 * There is one single task per each monitor object that runs.
 * The queue will have possibly many tasks per each monitor type.
 * Each monitor handles many MBeans, but only one attribute; however, both MBeans and attribute can be changed,
 * though it would be a strange way to use the monitor.
 *
 * @version $Revision: 1.6 $
 */
public abstract class MX4JMonitor extends StandardMBean implements MX4JMonitorMBean, MBeanRegistration, NotificationEmitter
{
   private static final TimeQueue queue = new TimeQueue();
   private static int references = 0;
   private static int sequenceNumber;

   private NotificationBroadcasterSupport emitter;
   private MBeanServer server;
   private boolean active;
   private List observeds = new ArrayList();
   private volatile String attribute;
   private volatile long granularity = 10 * 1000; // Spec says default is 10 seconds
   private boolean errorNotified;
   private final TimeTask task = new MonitorTask();
   private final Map infos = new HashMap();

   protected MX4JMonitor(Class management) throws NotCompliantMBeanException
   {
      super(management);
   }

   // TODO: override descriptions for this MBean

   public ObjectName preRegister(MBeanServer server, ObjectName name)
   {
      this.server = server;
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
   }

   public void preDeregister()
   {
      stop();
   }

   public void postDeregister()
   {
      server = null;
   }

   protected NotificationBroadcasterSupport createNotificationEmitter()
   {
      return new NotificationBroadcasterSupport();
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
   {
      emitter.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener);
   }

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      emitter.removeNotificationListener(listener, filter, handback);
   }

   public void sendNotification(Notification notification)
   {
      emitter.sendNotification(notification);
   }

   public synchronized void start()
   {
      if (isActive()) return;
      active = true;
      for (Iterator i = infos.values().iterator(); i.hasNext();)
      {
          MonitorInfo info = (MonitorInfo)i.next();
          info.clearNotificationStatus();
      }
      startMonitor();
   }

   public synchronized void stop()
   {
      if (!isActive()) return;
      active = false;
      stopMonitor();
   }

   public synchronized boolean isActive()
   {
      return active;
   }

   public synchronized void addObservedObject(ObjectName name) throws IllegalArgumentException
   {
      if (name == null) throw new IllegalArgumentException("Observed ObjectName cannot be null");
      if (!containsObservedObject(name))
      {
         observeds.add(name);
         putMonitorInfo(name, createMonitorInfo());
      }
   }

   public synchronized void removeObservedObject(ObjectName name)
   {
      observeds.remove(name);
      removeMonitorInfo(name);
   }

   public synchronized boolean containsObservedObject(ObjectName name)
   {
      return observeds.contains(name);
   }

   public synchronized ObjectName[] getObservedObjects()
   {
      return (ObjectName[])observeds.toArray(new ObjectName[observeds.size()]);
   }

   public synchronized void clearObservedObjects()
   {
      observeds.clear();
   }

   public synchronized String getObservedAttribute()
   {
      return attribute;
   }

   public synchronized void setObservedAttribute(String attribute)
   {
      this.attribute = attribute;
   }

   public synchronized long getGranularityPeriod()
   {
      return granularity;
   }

   public synchronized void setGranularityPeriod(long granularity) throws IllegalArgumentException
   {
      if (granularity <= 0) throw new IllegalArgumentException("Granularity must be greater than zero");
      this.granularity = granularity;
   }

   protected void startMonitor()
   {
      synchronized (queue)
      {
         if (references == 0) queue.start();
         ++references;
      }

      if (emitter == null) this.emitter = createNotificationEmitter();
      queue.schedule(task);
   }

   protected void stopMonitor()
   {
      queue.unschedule(task);

      synchronized (queue)
      {
         if (--references == 0) queue.stop();
      }
   }

   protected Logger getLogger()
   {
      return Log.getLogger(getClass().getName());
   }

   protected void sendNotification(String type, String message, ObjectName name, String attribute, Object gauge, Object trigger)
   {
      int sequence = 0;
      synchronized (MX4JMonitor.class)
      {
         sequence = ++sequenceNumber;
      }

      Notification notification = createMonitorNotification(type, sequence, message, name, attribute, gauge, trigger);
      sendNotification(notification);
   }

   protected Notification createMonitorNotification(String type, long sequence, String message, ObjectName observed, String attribute, Object gauge, Object trigger)
   {
      return new MX4JMonitorNotification(type, this, sequence, System.currentTimeMillis(), message, observed, attribute, gauge, trigger);
   }

   protected abstract void monitor(ObjectName name, String attribute, Object value, MonitorInfo info);

   protected abstract MonitorInfo createMonitorInfo();

   protected synchronized MonitorInfo getMonitorInfo(ObjectName name)
   {
      return (MonitorInfo)infos.get(name);
   }

   protected synchronized void putMonitorInfo(ObjectName name, MonitorInfo info)
   {
      infos.put(name, info);
   }

   protected synchronized void removeMonitorInfo(ObjectName name)
   {
      infos.remove(name);
   }

   protected void sendErrorNotification(MonitorInfo info, String type, String message, ObjectName observed, String attribute)
   {
      if (!info.isErrorNotified())
      {
         info.setErrorNotified(true);
         sendNotification(type, message, observed, attribute, null, null);
      }
   }

   private class MonitorTask extends TimeTask
   {
      protected boolean isPeriodic()
      {
         return true;
      }

      protected long getPeriod()
      {
         return getGranularityPeriod();
      }

      public boolean getFixedRate()
      {
         return true;
      }

      public void run()
      {
         if (!isActive()) return;

         long start = System.currentTimeMillis();

         String attribute = getObservedAttribute();

         if (server == null)
         {
            if (!errorNotified)
            {
               errorNotified = true;
               sendNotification(MonitorNotification.RUNTIME_ERROR, "Monitors must be registered in the MBeanServer", null, attribute, null, null);
            }
         }
         else
         {
            errorNotified = false;

            // If no attribute, sleep and try again
            if (attribute != null)
            {
               ObjectName[] names = getObservedObjects();
               // If no names, sleep and try again
               for (int i = 0; i < names.length; i++)
               {
                  ObjectName name = names[i];
                  MonitorInfo info = getMonitorInfo(name);
                  if (info == null) continue;
                  try
                  {
                     Object value = server.getAttribute(name, attribute);
                     // If no value, sleep and try again
                     if (value != null)
                     {
                        monitor(name, attribute, value, info);
                     }
                  }
                  catch (InstanceNotFoundException x)
                  {
                     sendErrorNotification(info, MonitorNotification.OBSERVED_OBJECT_ERROR, "Could not find observed MBean", name, attribute);
                  }
                  catch (AttributeNotFoundException x)
                  {
                     sendErrorNotification(info, MonitorNotification.OBSERVED_ATTRIBUTE_ERROR, "Could not find observed attribute " + attribute, name, attribute);
                  }
                  catch (MBeanException x)
                  {
                     sendErrorNotification(info, MonitorNotification.RUNTIME_ERROR, x.toString(), name, attribute);
                  }
                  catch (ReflectionException x)
                  {
                     sendErrorNotification(info, MonitorNotification.RUNTIME_ERROR, x.toString(), name, attribute);
                  }
               }
            }
         }

         long end = System.currentTimeMillis();
         long elapsed = end - start;
         Logger logger = getLogger();
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Monitored attribute " + attribute + " in " + elapsed + " ms");
      }
   }

   protected class MonitorInfo
   {
      private boolean errorNotified;

      public boolean isErrorNotified()
      {
         return errorNotified;
      }

      public void setErrorNotified(boolean errorNotified)
      {
         this.errorNotified = errorNotified;
      }

      public String toString()
      {
         return "errorNotified=" + isErrorNotified();
      }

      public void clearNotificationStatus()
      {
         errorNotified = false;
      }
   }
}
