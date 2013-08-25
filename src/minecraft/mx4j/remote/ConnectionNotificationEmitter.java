/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import javax.management.NotificationBroadcasterSupport;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;

/**
 * @version $Revision: 1.4 $
 */
public class ConnectionNotificationEmitter extends NotificationBroadcasterSupport
{
   private static long sequenceNumber;

   private JMXConnector connector;

   public ConnectionNotificationEmitter(JMXConnector connector)
   {
      this.connector = connector;
   }

   private long getNextNotificationNumber()
   {
      synchronized (ConnectionNotificationEmitter.class)
      {
         return sequenceNumber++;
      }
   }

   private String getConnectionId()
   {
      try
      {
         return connector.getConnectionId();
      }
      catch (IOException x)
      {
         return null;
      }
   }

   public void sendConnectionNotificationOpened()
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.OPENED, connector, getConnectionId(), getNextNotificationNumber(), "Connection opened", null);
      sendNotification(notification);
   }

   public void sendConnectionNotificationClosed()
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.CLOSED, connector, getConnectionId(), getNextNotificationNumber(), "Connection closed", null);
      sendNotification(notification);
   }

   public void sendConnectionNotificationFailed()
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.FAILED, connector, getConnectionId(), getNextNotificationNumber(), "Connection failed", null);
      sendNotification(notification);
   }

   public void sendConnectionNotificationLost(long howMany)
   {
      JMXConnectionNotification notification = new JMXConnectionNotification(JMXConnectionNotification.NOTIFS_LOST, connector, getConnectionId(), getNextNotificationNumber(), "Some notification (" + howMany + ") was lost", null);
      sendNotification(notification);
   }
}
