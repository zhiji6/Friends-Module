/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.monitor;

import javax.management.Notification;
import javax.management.ObjectName;

/**
 * @version $Revision: 1.3 $
 */
public class MX4JMonitorNotification extends Notification
{
   private final ObjectName observedObject;
   private final String observedAttribute;
   private final Object derivedGauge;
   private final Object trigger;

   public MX4JMonitorNotification(String type, Object source, long sequenceNumber, long timeStamp, String message, ObjectName monitoredName, String attribute, Object gauge, Object trigger)
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
