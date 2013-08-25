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

import mx4j.monitor.MX4JGaugeMonitor;
import mx4j.monitor.MX4JMonitor;

/**
 * @version $Revision: 1.13 $
 */
public class GaugeMonitor extends Monitor implements GaugeMonitorMBean
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
                 MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED,
                 MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED
              },
                                        MonitorNotification.class.getName(),
                                        "Notifications sent by the GaugeMonitor MBean")
           };

   MX4JMonitor createMX4JMonitor()
   {
      try
      {
         return new MX4JGaugeMonitor()
         {
            protected NotificationBroadcasterSupport createNotificationEmitter()
            {
               return GaugeMonitor.this;
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

   public Number getDerivedGauge(ObjectName objectName)
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getDerivedGauge(objectName);
   }

   public long getDerivedGaugeTimeStamp(ObjectName objectName)
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getDerivedGaugeTimeStamp(objectName);
   }

   public Number getHighThreshold()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getHighThreshold();
   }

   public Number getLowThreshold()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getLowThreshold();
   }

   public void setThresholds(Number highValue, Number lowValue) throws IllegalArgumentException
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      monitor.setThresholds(highValue, lowValue);
   }

   public boolean getNotifyHigh()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getNotifyHigh();
   }

   public void setNotifyHigh(boolean value)
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      monitor.setNotifyHigh(value);
   }

   public boolean getNotifyLow()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getNotifyLow();
   }

   public void setNotifyLow(boolean value)
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      monitor.setNotifyLow(value);
   }

   public boolean getDifferenceMode()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getDifferenceMode();
   }

   public void setDifferenceMode(boolean value)
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      monitor.setDifferenceMode(value);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      MX4JGaugeMonitor monitor = (MX4JGaugeMonitor)getMX4JMonitor();
      return monitor.getNotificationInfo();
   }
}
