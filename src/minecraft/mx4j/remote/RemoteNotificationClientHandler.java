/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

/**
 * Handles remote notification fetching on client side.
 * It takes care of calling the server side with the correct protocol and to dispatch
 * notifications to client-side listeners.
 *
 * @version $Revision: 1.10 $
 * @see RemoteNotificationServerHandler
 */
public interface RemoteNotificationClientHandler
{
   /**
    * Starts notification fetching
    *
    * @see #stop
    */
   public void start();

   /**
    * Stops notification fetching
    *
    * @see #start
    */
   public void stop();

   /**
    * Returns whether the tuple is already present in this handler
    */
   public boolean contains(NotificationTuple tuple);

   /**
    * Adds the given tuple with the given listener ID to this handler
    *
    * @see javax.management.MBeanServerConnection#addNotificationListener
    * @see #removeNotificationListeners
    */
   public void addNotificationListener(Integer id, NotificationTuple tuple);

   /**
    * Returns the IDs of the listeners for the given tuple
    *
    * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener)
    * @see #getNotificationListener
    */
   public Integer[] getNotificationListeners(NotificationTuple tuple);

   /**
    * Returns the ID of the listener for the given tuple
    *
    * @see javax.management.MBeanServerConnection#removeNotificationListener(javax.management.ObjectName, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
    * @see #getNotificationListeners
    */
   public Integer getNotificationListener(NotificationTuple tuple);

   /**
    * Removes the listeners with the given IDs from this handler
    *
    * @see #addNotificationListener
    */
   public void removeNotificationListeners(Integer[] ids);
}
