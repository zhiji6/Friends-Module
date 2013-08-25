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

import mx4j.monitor.MX4JMonitor;
import mx4j.monitor.MX4JStringMonitor;

/**
 * @version $Revision: 1.8 $
 */
public class StringMonitor extends Monitor implements StringMonitorMBean
{
   private static final MBeanNotificationInfo[] notificationInfos =
           {
              new MBeanNotificationInfo(new String[]
              {
                 javax.management.monitor.MonitorNotification.RUNTIME_ERROR,
                 MonitorNotification.OBSERVED_OBJECT_ERROR,
                 MonitorNotification.OBSERVED_ATTRIBUTE_ERROR,
                 MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR,
                 MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED,
                 MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED
              }
                                        , MonitorNotification.class.getName(),
                                        "Notifications sent by the StringMonitor MBean")
           };

   MX4JMonitor createMX4JMonitor()
   {
      try
      {
         return new MX4JStringMonitor()
         {
            protected NotificationBroadcasterSupport createNotificationEmitter()
            {
               return StringMonitor.this;
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
   public String getDerivedGauge()
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

   public String getDerivedGauge(ObjectName objectName)
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getDerivedGauge(objectName);
   }

   public long getDerivedGaugeTimeStamp(ObjectName objectName)
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getDerivedGaugeTimeStamp(objectName);
   }

   public String getStringToCompare()
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getStringToCompare();
   }

   public void setStringToCompare(String value) throws IllegalArgumentException
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      monitor.setStringToCompare(value);
   }

   public boolean getNotifyMatch()
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getNotifyMatch();
   }

   public void setNotifyMatch(boolean value)
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      monitor.setNotifyMatch(value);
   }

   public boolean getNotifyDiffer()
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getNotifyDiffer();
   }

   public void setNotifyDiffer(boolean value)
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      monitor.setNotifyDiffer(value);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      MX4JStringMonitor monitor = (MX4JStringMonitor)getMX4JMonitor();
      return monitor.getNotificationInfo();
   }
}
