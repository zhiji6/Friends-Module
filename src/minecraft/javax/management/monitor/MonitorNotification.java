/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.monitor;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.8 $
 */
public class MonitorNotification extends Notification
{
   private static final long serialVersionUID = -4608189663661929204L;

   public static final String OBSERVED_ATTRIBUTE_ERROR = "jmx.monitor.error.attribute";
   public static final String OBSERVED_ATTRIBUTE_TYPE_ERROR = "jmx.monitor.error.type";
   public static final String OBSERVED_OBJECT_ERROR = "jmx.monitor.error.mbean";
   public static final String RUNTIME_ERROR = "jmx.monitor.error.runtime";
   public static final String STRING_TO_COMPARE_VALUE_DIFFERED = "jmx.monitor.string.differs";
   public static final String STRING_TO_COMPARE_VALUE_MATCHED = "jmx.monitor.string.matches";
   public static final String THRESHOLD_ERROR = "jmx.monitor.error.threshold";
   public static final String THRESHOLD_HIGH_VALUE_EXCEEDED = "jmx.monitor.gauge.high";
   public static final String THRESHOLD_LOW_VALUE_EXCEEDED = "jmx.monitor.gauge.low";
   public static final String THRESHOLD_VALUE_EXCEEDED = "jmx.monitor.counter.threshold";

   private final ObjectName observedObject;
   private final String observedAttribute;
   private final Object derivedGauge;
   private final Object trigger;

   MonitorNotification(String type, Object source, long sequenceNumber, long timeStamp, String message, ObjectName monitoredName, String attribute, Object gauge, Object trigger)
   {
      super(type, source, sequenceNumber, timeStamp, message);
      this.observedObject = monitoredName;
      this.observedAttribute = attribute;
      this.derivedGauge = gauge;
      this.trigger = trigger;
   }

   public ObjectName getObservedObject()
   {
      return observedObject;
   }

   public Object getDerivedGauge()
   {
      return derivedGauge;
   }

   public String getObservedAttribute()
   {
      return observedAttribute;
   }

   public Object getTrigger()
   {
      return trigger;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("[");
      buffer.append(super.toString()).append(", ");
      buffer.append("observed=").append(getObservedObject()).append(", ");
      buffer.append("gauge=").append(getDerivedGauge()).append(", ");
      buffer.append("attribute=").append(getObservedAttribute()).append(", ");
      buffer.append("trigger=").append(getTrigger()).append("]");
      return buffer.toString();
   }
}
