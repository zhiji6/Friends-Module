/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * The notification emitted by the {@link MBeanServerDelegate delegate MBean}
 * when an MBean is registered or unregistered.
 *
 * @version $Revision: 1.9 $
 */
public class MBeanServerNotification extends Notification
{
   private static final long serialVersionUID = 2876477500475969677L;

   /**
    * The type of the notification when an MBean is registered
    */
   public static final String REGISTRATION_NOTIFICATION = "JMX.mbean.registered";
   /**
    * The type of the notification when an MBean is unregistered
    */
   public static final String UNREGISTRATION_NOTIFICATION = "JMX.mbean.unregistered";

   /**
    * @serial The ObjectName of the MBean that is registered or unregistered
    */
   private ObjectName objectName;

   /**
    * Creates a new MBeanServerNotification.
    *
    * @param type           Either REGISTRATION_NOTIFICATION or UNREGISTRATION_NOTIFICATION
    * @param source         The MBeanServerDelegate's ObjectName
    * @param sequenceNumber A sequence number
    * @param objectName     The ObjectName of the MBean registered or unregistered
    */
   public MBeanServerNotification(String type, Object source, long sequenceNumber, ObjectName objectName)
   {
      super(type, source, sequenceNumber, "");
      if (!type.equals(REGISTRATION_NOTIFICATION) && !type.equals(UNREGISTRATION_NOTIFICATION))
      {
         throw new RuntimeOperationsException(new IllegalArgumentException("Bad notification type for MBeanServerNotification"));
      }
      this.objectName = objectName;
   }

   /**
    * Returns the ObjectName of the MBean that was registered or unregistered
    */
   public ObjectName getMBeanName()
   {
      return objectName;
   }

   public String toString()
   {
      StringBuffer b = new StringBuffer(super.toString());
      b.append("[");
      b.append(getMBeanName());
      b.append("]");
      return b.toString();
   }
}
