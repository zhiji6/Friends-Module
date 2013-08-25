/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import java.math.BigDecimal;
import java.math.BigInteger;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;

import mx4j.log.Logger;

/**
 * @version $Revision: 1.3 $
 */
public class MX4JGaugeMonitor extends MX4JMonitor implements MX4JGaugeMonitorMBean
{
   private static Integer ZERO = new Integer(0);

   private Number highThreshold = ZERO;
   private Number lowThreshold = ZERO;
   private boolean notifyHigh;
   private boolean notifyLow;
   private boolean differenceMode;

   public MX4JGaugeMonitor() throws NotCompliantMBeanException
   {
      super(MX4JGaugeMonitorMBean.class);
   }

   protected MX4JGaugeMonitor(Class management) throws NotCompliantMBeanException
   {
      super(management);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // TODO
      return new MBeanNotificationInfo[0];
   }

   public synchronized Number getHighThreshold()
   {
      return highThreshold;
   }

   public synchronized Number getLowThreshold()
   {
      return lowThreshold;
   }

   public void setThresholds(Number highValue, Number lowValue) throws IllegalArgumentException
   {
      if (highValue == null) throw new IllegalArgumentException("High Threshold cannot be null");
      if (lowValue == null) throw new IllegalArgumentException("Low Threshold cannot be null");
      if (highValue.getClass() != lowValue.getClass()) throw new IllegalArgumentException("Thresholds must be of the same type");
      if (compare(highValue, lowValue) < 0) throw new IllegalArgumentException("High threshold cannot be lower than low threshold");
      highThreshold = highValue;
      lowThreshold = lowValue;
   }

   public synchronized boolean getNotifyHigh()
   {
      return notifyHigh;
   }

   public synchronized boolean getNotifyLow()
   {
      return notifyLow;
   }

   public synchronized void setNotifyHigh(boolean notifyHigh)
   {
      this.notifyHigh = notifyHigh;
   }

   public synchronized void setNotifyLow(boolean notifyLow)
   {
      this.notifyLow = notifyLow;
   }

   public synchronized boolean getDifferenceMode()
   {
      return differenceMode;
   }

   public synchronized void setDifferenceMode(boolean differenceMode)
   {
      this.differenceMode = differenceMode;
   }

   public Number getDerivedGauge(ObjectName objectName)
   {
      GaugeMonitorInfo info = (GaugeMonitorInfo)getMonitorInfo(objectName);
      return info.getGauge();
   }

   public long getDerivedGaugeTimeStamp(ObjectName objectName)
   {
      GaugeMonitorInfo info = (GaugeMonitorInfo)getMonitorInfo(objectName);
      return info.getTimestamp();
   }

   protected MonitorInfo createMonitorInfo()
   {
      return new GaugeMonitorInfo();
   }

   protected int compare(Number left, Number right)
   {
      if (left instanceof BigDecimal && right instanceof BigDecimal) return ((BigDecimal)left).compareTo((BigDecimal)right);
      if (left instanceof BigInteger && right instanceof BigInteger) return ((BigInteger)left).compareTo((BigInteger)right);
      return new Double(left.doubleValue()).compareTo(new Double(right.doubleValue()));
   }

   protected Number sub(Number left, Number right)
   {
      if (left instanceof BigDecimal && right instanceof BigDecimal) return ((BigDecimal)left).subtract((BigDecimal)right);
      if (left instanceof BigDecimal) return ((BigDecimal)left).subtract(new BigDecimal(right.doubleValue()));
      if (left instanceof BigInteger && right instanceof BigInteger) return ((BigInteger)left).subtract((BigInteger)right);
      if (left instanceof BigInteger) return ((BigInteger)left).subtract(BigInteger.valueOf(right.longValue()));
      if (left instanceof Double || right instanceof Double) return new Double(left.doubleValue() - right.doubleValue());
      if (left instanceof Float || right instanceof Float) return new Float(left.floatValue() - right.floatValue());
      if (left instanceof Long || right instanceof Long) return new Long(left.longValue() - right.longValue());
      if (left instanceof Integer || right instanceof Integer) return new Integer(left.intValue() - right.intValue());
      if (left instanceof Short || right instanceof Short) return new Short((short)(left.shortValue() - right.shortValue()));
      if (left instanceof Byte || right instanceof Byte) return new Byte((byte)(left.byteValue() - right.byteValue()));
      return null;
   }

   protected void monitor(ObjectName name, String attribute, Object value, MonitorInfo monitorInfo)
   {
      if (!(value instanceof Number))
      {
         sendErrorNotification(monitorInfo, MonitorNotification.OBSERVED_ATTRIBUTE_TYPE_ERROR, "Attribute type must be a Number, not " + value.getClass(), name, attribute);
         return;
      }

      Number gauge = (Number)value;

      // Spec requires that types of gauge, high threshold and low threshold be affine
      Number high = null;
      Number low = null;
      synchronized (this)
      {
         high = getHighThreshold();
         low = getLowThreshold();
      }
      Class gaugeClass = gauge.getClass();
      if (high != ZERO && high.getClass() != gaugeClass)
      {
         sendErrorNotification(monitorInfo, MonitorNotification.THRESHOLD_ERROR, "Threshold type " + high.getClass() + " must be of same type of the attribute " + gaugeClass, name, attribute);
         return;
      }
      if (low != ZERO && low.getClass() != gaugeClass)
      {
         sendErrorNotification(monitorInfo, MonitorNotification.THRESHOLD_ERROR, "Offset type " + low.getClass() + " must be of same type of the attribute " + gaugeClass, name, attribute);
         return;
      }

      Logger logger = getLogger();

      // Contains previous gauge
      GaugeMonitorInfo info = (GaugeMonitorInfo)monitorInfo;
      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Computing gauge, previous values are: " + info);
         logger.debug("Current values are: gauge=" + gauge + ", highThreshold=" + high + ", lowThreshold=" + low);
      }

      if (getDifferenceMode())
      {
         Number diffGauge = sub(gauge, info.getGauge());
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("CounterMonitor in difference mode, difference gauge=" + diffGauge);
         compareAndSendNotification(diffGauge, low, high, info, name, attribute);
      }
      else
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("CounterMonitor in absolute mode, gauge=" + gauge);
         compareAndSendNotification(gauge, low, high, info, name, attribute);
      }

      info.setGauge(gauge);
      info.setTimestamp(System.currentTimeMillis());
   }

   private void compareAndSendNotification(Number gauge, Number low, Number high, GaugeMonitorInfo info, ObjectName name, String attribute)
   {
      Logger logger = getLogger();

      if (info.isHighNotified() && compare(gauge, low) > 0)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("High threshold " + high + " already notified, gauge " + gauge + " not below low threshold " + low);
         return;
      }
      if (info.isLowNotified() && compare(gauge, high) < 0)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Low threshold " + low + " already notified, gauge " + gauge + " not above high threshold " + high);
         return;
      }

      if (compare(gauge, high) >= 0)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Gauge above high threshold: gauge=" + gauge + ", high threshold=" + high + ", low threshold=" + low);
         info.setLowNotified(false);
         if (getNotifyHigh())
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending high threshold exceeded notification");
            info.setHighNotified(true);
            sendNotification(MonitorNotification.THRESHOLD_HIGH_VALUE_EXCEEDED, "High threshold " + high + " exceeded: " + gauge, name, attribute, gauge, high);
         }
         else
         {
            info.setHighNotified(false);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("GaugeMonitor is configured in non-high-notification mode");
         }
      }
      else if (compare(gauge, low) <= 0)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Gauge below low threshold: gauge=" + gauge + ", low threshold=" + low + ", high threshold=" + high);
         info.setHighNotified(false);
         if (getNotifyLow())
         {
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending low threshold exceeded notification");
            info.setLowNotified(true);
            sendNotification(MonitorNotification.THRESHOLD_LOW_VALUE_EXCEEDED, "Low threshold " + low + " exceeded: " + gauge, name, attribute, gauge, low);
         }
         else
         {
            info.setLowNotified(false);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("GaugeMonitor is configured in non-low-notification mode");
         }
      }
      else
      {
         info.setHighNotified(false);
         info.setLowNotified(false);
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Gauge between thresholds: gauge=" + gauge + ", low threshold=" + low + ", high threshold=" + high);
      }
   }

   protected class GaugeMonitorInfo extends MonitorInfo
   {
      private Number gauge = ZERO;
      private long timestamp;
      private boolean highNotified;
      private boolean lowNotified;

      public Number getGauge()
      {
         return gauge;
      }

      public void setGauge(Number gauge)
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

      public boolean isHighNotified()
      {
         return highNotified;
      }

      public void setHighNotified(boolean highNotified)
      {
         this.highNotified = highNotified;
      }

      public boolean isLowNotified()
      {
         return lowNotified;
      }

      public void setLowNotified(boolean lowNotified)
      {
         this.lowNotified = lowNotified;
      }

      public void clearNotificationStatus() {
         super.clearNotificationStatus();
         highNotified = false;
         lowNotified = false;
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer(super.toString());
         buffer.append(", gauge=").append(getGauge());
         buffer.append(", lowNotified=").append(isLowNotified());
         buffer.append(", highNotified=").append(isHighNotified());
         return buffer.toString();
      }
   }
}
