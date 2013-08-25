/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.monitor;

import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import mx4j.monitor.MX4JCounterMonitor;
import mx4j.monitor.MX4JMonitor;

/**
 * @version $Revision: 1.9 $
 */
public class CounterMonitor extends Monitor implements CounterMonitorMBean
{
   private static final MBeanNotificationInfo[] notificationInfos =
           {
              new MBeanNotificationInfo(new String[]
              {
                 MonitorNotification.RUNTIME_ERROR,
                 MonitorNotification.OBSERVED_OBJECT_ERROR,
                 MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
                 MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                 MonitorNotification.THRESHOLD_ERROR,
                 MonitorNotification.THRESHOLD_VALUE_EXCEEDED
              },
                                        MonitorNotification.class.getName(),
                                        "Notifications sent by the CounterMonitor MBean")
           };

   MX4JMonitor createMX4JMonitor()
   {
      try
      {
         return new MX4JCounterMonitor()
         {
            protected NotificationBroadcasterSupport createNotificationEmitter()
            {
               return CounterMonitor.this;
            }

            public MBeanNotificationInfo[] getNotificationInfo()
            {
               return notificationInfos;
            }

            protected Notification createMonitorNotification(String type, long sequence, String message, ObjectName observed, String attribute, Object gauge, Object trigger)
            {
               return new MonitorNotification(type, this, sequence, System.currentTimeMillis(), message, observed, attribute, gauge, trigger);
            }
         };
      }
      catch (NotCompliantMBeanException x)
      {
         return null;
      }
   }

   public void start()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.start();
   }

   public void stop()
   {
      MX4JMonitor monitor = getMX4JMonitor();
      monitor.stop();
   }

   /**
    * @deprecated
    */
   public Number getDerivedGauge()
   {
      return getDerivedGauge(getObservedObject());
   }

   /**
    * @deprecated
    */
   public long getDerivedGaugeTimeStamp()
   {
      return getDerivedGaugeTimeStamp(getObservedObject());
   }

   /**
    * @deprecated
    */
   public Number getThreshold()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getThreshold(getObservedObject());
   }

   /**
    * @deprecated
    */
   public void setThreshold(Number value) throws java.lang.IllegalArgumentException
   {
      setInitThreshold(value);
   }

   public Number getDerivedGauge(ObjectName objectName)
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getDerivedGauge(objectName);
   }

   public long getDerivedGaugeTimeStamp(ObjectName objectName)
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getDerivedGaugeTimeStamp(objectName);
   }

   public Number getThreshold(ObjectName objectName)
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getThreshold(objectName);
   }

   public Number getInitThreshold()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getInitThreshold();
   }

   public void setInitThreshold(Number value) throws java.lang.IllegalArgumentException
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      monitor.setInitThreshold(value);
   }

   public Number getOffset()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getOffset();
   }

   public synchronized void setOffset(Number value) throws java.lang.IllegalArgumentException
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      monitor.setOffset(value);
   }

   public Number getModulus()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getModulus();
   }

   public void setModulus(Number value) throws java.lang.IllegalArgumentException
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      monitor.setModulus(value);
   }

   public boolean getNotify()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getNotify();
   }

   public void setNotify(boolean value)
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      monitor.setNotify(value);
   }

   public boolean getDifferenceMode()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getDifferenceMode();
   }

   public void setDifferenceMode(boolean value)
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      monitor.setDifferenceMode(value);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      MX4JCounterMonitor monitor = (MX4JCounterMonitor)getMX4JMonitor();
      return monitor.getNotificationInfo();
   }
}
