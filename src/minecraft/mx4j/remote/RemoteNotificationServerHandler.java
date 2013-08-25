/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.remote.NotificationResult;

/**
 * Handles remote notification sending on server side.
 * It takes care of collecting requests for listener addition and removal, and to send
 * notifications back to the client side.
 *
 * @version $Revision: 1.7 $
 * @see RemoteNotificationClientHandler
 */
public interface RemoteNotificationServerHandler
{
   /**
    * Returns a unique ID for a client-side NotificationListener
    *
    * @see #addNotificationListener
    */
   public Integer generateListenerID(ObjectName name, NotificationFilter filter);

   /**
    * Returns the unique server side listener that will represent client-side listeners
    * on MBeans.
    */
   public NotificationListener getServerNotificationListener();

   /**
    * Adds the given tuple with the given listener ID to this handler
    *
    * @see #removeNotificationListener
    */
   public void addNotificationListener(Integer id, NotificationTuple tuple);

   /**
    * Removes the listener with the given ID from this handler
    *
    * @see #addNotificationListener
    */
   public NotificationTuple removeNotificationListener(Integer id);

   /**
    * Fetches notifications from the notification buffer in order to send them
    * to the client side
    *
    * @throws IOException If this handler has already been closed
    */
   public NotificationResult fetchNotifications(long sequenceNumber, int maxNotifications, long timeout) throws IOException;

   /**
    * Closes this handler, that will not accept anymore add or removal of listeners
    *
    * @return The NotificationTuples currently held
    * @see #fetchNotifications
    */
   public NotificationTuple[] close();
}
