/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

/**
 * @version $Revision: 1.4 $
 */
public class NotificationResult implements Serializable
{
   private static final long serialVersionUID = 1191800228721395279l;

   /**
    * @serial The earliest sequence number
    */
   private final long earliestSequenceNumber;
   /**
    * @serial The next sequence number
    */
   private final long nextSequenceNumber;
   /**
    * @serial The notifications
    */
   private final TargetedNotification[] targetedNotifications;

   public NotificationResult(long earliestSequenceNumber, long nextSequenceNumber, TargetedNotification[] targetedNotifications)
   {
      // Checks required by the specification (javadocs)
      if (earliestSequenceNumber < 0) throw new IllegalArgumentException("Earliest sequence number cannot be negative");
      if (nextSequenceNumber < 0) throw new IllegalArgumentException("Next sequence number cannot be negative");
      if (targetedNotifications == null) throw new IllegalArgumentException("TargetedNotification array cannot be null");
      this.earliestSequenceNumber = earliestSequenceNumber;
      this.nextSequenceNumber = nextSequenceNumber;
      this.targetedNotifications = targetedNotifications;
   }

   public long getEarliestSequenceNumber()
   {
      return earliestSequenceNumber;
   }

   public long getNextSequenceNumber()
   {
      return nextSequenceNumber;
   }

   public TargetedNotification[] getTargetedNotifications()
   {
      return targetedNotifications;
   }

   public String toString()
   {
      StringBuffer buffer = new StringBuffer("NotificationResult[earliest=");
      buffer.append(getEarliestSequenceNumber()).append(", next=");
      buffer.append(getNextSequenceNumber()).append(", notifications=");
      TargetedNotification[] notifs = getTargetedNotifications();
      List list = notifs == null ? null : Arrays.asList(notifs);
      buffer.append(list).append("]");
      return buffer.toString();
   }
}
