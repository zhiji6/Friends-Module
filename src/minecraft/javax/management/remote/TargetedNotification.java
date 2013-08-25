/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.Serializable;
import javax.management.Notification;

/**
 * @version $Revision: 1.4 $
 */
public class TargetedNotification implements Serializable
{
   /**
    * @serial The notification to transmit
    */
   private final Notification notif;
   /**
    * @serial The listener's ID for the notification
    */
   private final Integer id;

   public TargetedNotification(Notification notif, Integer id)
   {
      this.notif = notif;
      this.id = id;
   }

   public Notification getNotification()
   {
      return notif;
   }

   public Integer getListenerID()
   {
      return id;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("TargetedNotification[id=");
      buffer.append(getListenerID()).append(", notification=");
      buffer.append(getNotification()).append("]");
      return buffer.toString();
   }
}
