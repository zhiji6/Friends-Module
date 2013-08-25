/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;

import mx4j.log.Logger;

/**
 * @version $Revision: 1.3 $
 */
public class MX4JStringMonitor extends MX4JMonitor implements MX4JStringMonitorMBean
{
   private static final String EMPTY = "";

   private String stringToCompare = EMPTY;
   private boolean notifyMatch;
   private boolean notifyDiffer;

   public MX4JStringMonitor() throws NotCompliantMBeanException
   {
      super(MX4JStringMonitorMBean.class);
   }

   public MX4JStringMonitor(Class management) throws NotCompliantMBeanException
   {
      super(management);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // TODO
      return new MBeanNotificationInfo[0];
   }

   public synchronized String getStringToCompare()
   {
      return stringToCompare;
   }

   public synchronized void setStringToCompare(String value) throws IllegalArgumentException
   {
      if (value == null) throw new IllegalArgumentException("String to compare cannot be null");
      this.stringToCompare = value;
   }

   public synchronized boolean getNotifyMatch()
   {
      return notifyMatch;
   }

   public synchronized void setNotifyMatch(boolean notifyMatch)
   {
      this.notifyMatch = notifyMatch;
   }

   public synchronized boolean getNotifyDiffer()
   {
      return notifyDiffer;
   }

   public synchronized void setNotifyDiffer(boolean notifyDiffer)
   {
      this.notifyDiffer = notifyDiffer;
   }

   public String getDerivedGauge(ObjectName objectName)
   {
      StringMonitorInfo info = (StringMonitorInfo)getMonitorInfo(objectName);
      return info.getGauge();
   }

   public long getDerivedGaugeTimeStamp(ObjectName objectName)
   {
      StringMonitorInfo info = (StringMonitorInfo)getMonitorInfo(objectName);
      return info.getTimestamp();
   }

   protected MonitorInfo createMonitorInfo()
   {
      return new StringMonitorInfo();
   }

   protected int compare(String left, String right)
   {
      return left == null ? right == null ? 0 : -1 : right == null ? 1 : left.compareTo(right);
   }

   protected void monitor(ObjectName name, String attribute, Object value, MonitorInfo monitorInfo)
   {
      if (!(value instanceof String))
      {
         sendErrorNotification(monitorInfo, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, "Attribute type must be a String, not " + value.getClass(), name, attribute);
         return;
      }

      String gauge = (String)value;

      String reference = null;
      synchronized (this)
      {
         reference = getStringToCompare();
      }

      Logger logger = getLogger();

      StringMonitorInfo info = (StringMonitorInfo)monitorInfo;
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Computing gauge, previous values are: " + info);
         logger.debug("Current values are: gauge=" + gauge + ", stringToCompare=" + reference);
      }

      compareAndSendNotification(gauge, reference, info, name, attribute);

      info.setGauge(gauge);
      info.setTimestamp(System.currentTimeMillis());
   }

   private void compareAndSendNotification(String gauge, String reference, StringMonitorInfo info, ObjectName name, String attribute)
   {
      Logger logger = getLogger();

      boolean equals = compare(gauge, reference) == 0;

      if (info.isDifferNotified() && !equals)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Difference already notified, gauge=" + gauge + ", string-to-compare=" + reference);
         return;
      }
      if (info.isMatchNotified() && equals)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Match already notified, gauge=" + gauge + ", string-to-compare=" + reference);
         return;
      }

      if (equals)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Gauge matches, gauge=" + gauge + ", string-to-compare=" + reference);
         info.setDifferNotified(false);
         if (getNotifyMatch())
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending string match notification");
            info.setMatchNotified(true);
            sendNotification(MonitorNotification.STRING_TO_COMPARE_VALUE_MATCHED, "Gauge " + gauge + " matched " + reference, name, attribute, gauge, reference);
         }
         else
         {
            info.setMatchNotified(false);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("StringMonitor is configured in non-match-notification mode");
         }
      }
      else
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Gauge differs, gauge=" + gauge + ", string-to-compare=" + reference);
         info.setMatchNotified(false);
         if (getNotifyDiffer())
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending string differ notification");
            info.setDifferNotified(true);
            sendNotification(MonitorNotification.STRING_TO_COMPARE_VALUE_DIFFERED, "Gauge " + gauge + " differs from " + reference, name, attribute, gauge, reference);
         }
         else
         {
            info.setDifferNotified(false);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("StringMonitor is configured in non-differ-notification mode");
         }
      }
   }

   protected class StringMonitorInfo extends MonitorInfo
   {
      private String gauge;
      private long timestamp;
      private boolean matchNotified;
      private boolean differNotified;

      public String getGauge()
      {
         return gauge;
      }

      public void setGauge(String gauge)
      {
         this.gauge = gauge;
      }

      public long getTimestamp()
      {
         return timestamp;
      }

      public void setTimestamp(long timestamp)
      {
         this.timestamp = timestamp;
      }

      public boolean isMatchNotified()
      {
         return matchNotified;
      }

      public void setMatchNotified(boolean matchNotified)
      {
         this.matchNotified = matchNotified;
      }

      public boolean isDifferNotified()
      {
         return differNotified;
      }

      public void setDifferNotified(boolean differNotified)
      {
         this.differNotified = differNotified;
      }

      public void clearNotificationStatus()
      {
         super.clearNotificationStatus();
         differNotified = false;
         matchNotified = false;
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer(super.toString());
         buffer.append(", gauge=").append(getGauge());
         buffer.append(", matchNotified=").append(isMatchNotified());
         buffer.append(", differNotified=").append(isDifferNotified());
         return buffer.toString();
      }
   }
}
