/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import java.math.BigInteger;
import javax.management.MBeanNotificationInfo;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.monitor.MonitorNotification;

import mx4j.log.Logger;

/**
 * @version $Revision: 1.6 $
 */
public class MX4JCounterMonitor extends MX4JMonitor implements MX4JCounterMonitorMBean
{
   private static Integer ZERO = new Integer(0);

   private Number threshold = ZERO;
   private Number offset = ZERO;
   private Number modulus = ZERO;
   private boolean notify;
   private boolean differenceMode;

   public MX4JCounterMonitor() throws NotCompliantMBeanException
   {
      super(MX4JCounterMonitorMBean.class);
   }

   protected MX4JCounterMonitor(Class management) throws NotCompliantMBeanException
   {
      super(management);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      // TODO
      return new MBeanNotificationInfo[0];
   }

   public synchronized Number getInitThreshold()
   {
      return threshold;
   }

   public void setInitThreshold(Number threshold) throws IllegalArgumentException
   {
      if (threshold == null || compare(threshold, ZERO) < 0) throw new IllegalArgumentException("Threshold cannot be " + threshold);
      this.threshold = threshold;
   }

   public synchronized Number getOffset()
   {
      return offset;
   }

   public void setOffset(Number offset) throws IllegalArgumentException
   {
      if (offset == null || compare(offset, ZERO) < 0) throw new IllegalArgumentException("Offset cannot be " + offset);
      this.offset = offset;
   }

   public Number getModulus()
   {
      return modulus;
   }

   public void setModulus(Number modulus) throws IllegalArgumentException
   {
      if (modulus == null || compare(modulus, ZERO) < 0) throw new IllegalArgumentException("Modulus cannot be " + modulus);
      this.modulus = modulus;
   }

   public boolean getNotify()
   {
      return notify;
   }

   public void setNotify(boolean notify)
   {
      this.notify = notify;
   }

   public boolean getDifferenceMode()
   {
      return differenceMode;
   }

   public void setDifferenceMode(boolean mode)
   {
      this.differenceMode = mode;
   }

   public Number getDerivedGauge(ObjectName name)
   {
      CounterMonitorInfo info = (CounterMonitorInfo)getMonitorInfo(name);
      return info.getGauge();
   }

   public long getDerivedGaugeTimeStamp(ObjectName name)
   {
      CounterMonitorInfo info = (CounterMonitorInfo)getMonitorInfo(name);
      return info.getTimestamp();
   }

   public Number getThreshold(ObjectName name)
   {
      CounterMonitorInfo info = (CounterMonitorInfo)getMonitorInfo(name);
      return info.getThreshold();
   }

   protected int compare(Number left, Number right)
   {
      if (left instanceof BigInteger && right instanceof BigInteger) return ((BigInteger)left).compareTo((BigInteger)right);
      if (left.longValue() == right.longValue()) return 0;
      return left.longValue() > right.longValue() ? 1 : -1;
   }

   protected Number sum(Number left, Number right)
   {
      if (left instanceof BigInteger && right instanceof BigInteger) return ((BigInteger)left).add((BigInteger)right);
      if (left instanceof BigInteger) return ((BigInteger)left).add(BigInteger.valueOf(right.longValue()));
      if (right instanceof BigInteger) return ((BigInteger)right).add(BigInteger.valueOf(left.longValue()));
      if (left instanceof Long || right instanceof Long) return new Long(left.longValue() + right.longValue());
      if (left instanceof Integer || right instanceof Integer) return new Integer(left.intValue() + right.intValue());
      if (left instanceof Short || right instanceof Short) return new Short((short)(left.shortValue() + right.shortValue()));
      if (left instanceof Byte || right instanceof Byte) return new Byte((byte)(left.byteValue() + right.byteValue()));
      return null;
   }

   protected Number sub(Number left, Number right)
   {
      if (left instanceof BigInteger && right instanceof BigInteger) return ((BigInteger)left).subtract((BigInteger)right);
      if (left instanceof BigInteger) return ((BigInteger)left).subtract(BigInteger.valueOf(right.longValue()));
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

      // Spec requires that types of gauge, threshold, offset and modulus be affine
      Number threshold = null;
      Number offset = null;
      Number modulus = null;
      synchronized (this)
      {
         threshold = getThreshold(name);
         offset = getOffset();
         modulus = getModulus();
      }
      Number counter = (Number)value;
      Class gaugeClass = counter.getClass();
      if (threshold != ZERO && threshold.getClass() != gaugeClass)
      {
         sendErrorNotification(monitorInfo, MonitorNotification.THRESHOLD_ERROR, "Threshold type " + threshold.getClass() + " must be of same type of the attribute " + gaugeClass, name, attribute);
         return;
      }
      if (offset != ZERO && offset.getClass() != gaugeClass)
      {
         sendErrorNotification(monitorInfo, MonitorNotification.THRESHOLD_ERROR, "Offset type " + offset.getClass() + " must be of same type of the attribute " + gaugeClass, name, attribute);
         return;
      }
      if (modulus != ZERO && modulus.getClass() != gaugeClass)
      {
         sendErrorNotification(monitorInfo, MonitorNotification.THRESHOLD_ERROR, "Modulus type " + modulus.getClass() + " must be of same type of the attribute " + gaugeClass, name, attribute);
         return;
      }

      Logger logger = getLogger();

      // Contains previous gauge and threshold
      CounterMonitorInfo info = (CounterMonitorInfo)monitorInfo;

      // see if the counter rolled over (the value went down)
      Number lastCounter = info.getCounter();
      boolean rolledOver = (lastCounter != null) ? compare(counter, lastCounter) < 0 : false;

      // calculate V[t] using rules from spec
      Number vt;
      if (getDifferenceMode())
      {
         if (lastCounter == null)
         {
            // we had no previous sample so the value is ZERO
            vt = ZERO;
         }
         else
         {
            vt = sub(counter, lastCounter);
            if (rolledOver)
            {
               // the delta was negative so add the modulus
               vt = sum(vt, modulus);
            }

            // if we rolled over reset the threshold
            if (rolledOver)
            {
               threshold = getInitThreshold();
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Threshold has been rolled over, new value = " + threshold);
            }
         }
      }
      else
      {
         vt = counter;

         // if we rolled over and have a modulus that is greater than the threshold, reset it
         if (rolledOver && compare(modulus, ZERO) > 0 && compare(threshold, modulus) > 0)
         {
            threshold = getInitThreshold();
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Threshold has been rolled over, new value = " + threshold);
         }
      }

      if (logger.isEnabledFor(Logger.DEBUG))
      {
         logger.debug("Computing gauge, previous values are: " + info);
         logger.debug("Current values are: threshold=" + threshold + ", offset=" + offset + ", modulus=" + modulus);
         logger.debug("V[t] = " + vt + ", rolledOver = " + rolledOver);
      }

      info.setGauge(vt);


      boolean notified;
      if (compare(vt, threshold) >= 0)
      {
         if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Threshold exceeded: V[t]=" + vt + ", threshold=" + threshold);

         // send any notification that is needed
         if (getNotify())
         {
            if (info.isThresholdNotified())
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Threshold exceeded already notified");
            }
            else
            {
               if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Sending threshold exceeded notification");
               sendNotification(MonitorNotification.THRESHOLD_VALUE_EXCEEDED, "Threshold " + threshold + " exceeded: " + vt, name, attribute, counter, threshold);
            }
            notified = true;
         }
         else
         {
            notified = false;
         }

         // adjust the threshold upward
         if (compare(offset, ZERO) > 0)
         {
            do
            {
               threshold = sum(threshold, offset);
            } while (compare(vt, threshold) >= 0);
            if (logger.isEnabledFor(Logger.DEBUG)) logger.debug("Threshold has been offset, new value = " + threshold);

         }
      }
      else
      {
         // cancel any notification status
         notified = false;
      }

      CounterMonitorInfo newInfo = (CounterMonitorInfo)createMonitorInfo();
      newInfo.setThresholdNotified(notified);
      newInfo.setCounter(counter);
      newInfo.setGauge(vt);
      newInfo.setTimestamp(System.currentTimeMillis());
      newInfo.setThreshold(threshold);
      putMonitorInfo(name, newInfo);
   }

   protected MonitorInfo createMonitorInfo()
   {
      return new CounterMonitorInfo();
   }

   protected class CounterMonitorInfo extends MonitorInfo
   {
      private boolean thresholdNotified;
      private Number counter = null;
      private Number gauge = ZERO;
      private long timestamp;
      private Number threshold = ZERO;

      public void setThreshold(Number threshold)
      {
         this.threshold = threshold;
      }

      public Number getThreshold()
      {
         if (threshold == ZERO) return getInitThreshold();
         return threshold;
      }

      public void setThresholdNotified(boolean thresholdNotified)
      {
         this.thresholdNotified = thresholdNotified;
      }

      public boolean isThresholdNotified()
      {
         return thresholdNotified;
      }

      public void setCounter(Number counter)
      {
         this.counter = counter;
      }

      public Number getCounter()
      {
         return counter;
      }

      public void setGauge(Number gauge)
      {
         this.gauge = gauge;
      }

      public Number getGauge()
      {
         return gauge;
      }

      public void setTimestamp(long timestamp)
      {
         this.timestamp = timestamp;
      }

      public long getTimestamp()
      {
         return timestamp;
      }

      public void clearNotificationStatus()
      {
         super.clearNotificationStatus();
         thresholdNotified = false;
      }

      public String toString()
      {
         StringBuffer buffer = new StringBuffer(super.toString());
         buffer.append(", thresholdNotified=").append(isThresholdNotified());
         buffer.append(", gauge=").append(getGauge());
         buffer.append(", counter=").append(getCounter());
         buffer.append(", threshold=").append(threshold);
         return buffer.toString();
      }
   }
}
