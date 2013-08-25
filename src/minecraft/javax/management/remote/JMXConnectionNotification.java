/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management.remote;

import javax.management.Notification;

/**
 * @version $Revision: 1.6 $
 */
public class JMXConnectionNotification extends Notification
{
   public static final String OPENED = "jmx.remote.connection.opened";
   public static final String CLOSED = "jmx.remote.connection.closed";
   public static final String FAILED = "jmx.remote.connection.failed";
   public static final String NOTIFS_LOST = "jmx.remote.connection.notifs.lost";

   private static final long serialVersionUID = -2331308725952627538l;

   /**
    * @serial The connection ID for this notification
    */
   private String connectionId;

   public JMXConnectionNotification(String type, Object source, String connectionId, long sequenceNumber, String message, Object userData)
   {
      super(type, source, sequenceNumber, System.currentTimeMillis(), message);
      setUserData(userData);
      this.connectionId = connectionId;
   }

   public String getConnectionId()
   {
      return connectionId;
   }
}
