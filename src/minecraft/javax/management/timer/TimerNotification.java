/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.timer;

import javax.management.Notification;

/**
 * Notifications sent by the {@link Timer} class are instances of this class.
 *
 * @version $Revision: 1.10 $
 */
public class TimerNotification extends Notification
{
   private static final long serialVersionUID = 1798492029603825750L;

   private Integer notificationID;

   public TimerNotification(String type, Object source, long sequenceNumber, long timeStamp, String message, Integer id)
   {
      super(type, source, sequenceNumber, timeStamp, message);
      this.notificationID = id;
   }

   public Integer getNotificationID()
   {
      return notificationID;
   }

   public String toString()
   {
      StringBuffer b = new StringBuffer("[");
      b.append(super.toString());
      b.append(", notificationID=").append(notificationID);
      b.append("]");
      return b.toString();
   }
}
