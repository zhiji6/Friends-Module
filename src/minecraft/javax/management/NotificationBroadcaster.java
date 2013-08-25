/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Although not deprecated, this interface should not be used since JMX 1.2; use
 * {@link NotificationEmitter} instead.
 *
 * @version $Revision: 1.8 $
 * @see Notification
 */
public interface NotificationBroadcaster
{
   /**
    * Returns the metadata information associated with this emitter.
    */
   public MBeanNotificationInfo[] getNotificationInfo();

   /**
    * Adds a  notification listener to this emitter.
    *
    * @param listener The notification listener which will handle the notifications emitted.
    * @param filter   Filters notifications that the listener should receive; may be null, if no filtering is required.
    * @param handback An opaque object to be sent back to the listener when a notification is emitted.
    * @see #removeNotificationListener(NotificationListener)
    */
   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException;

   /**
    * Removes a notification listener from this emitter.
    * If the listener has been registered with different handback objects or notification filters,
    * all entries corresponding to the listener will be removed.
    *
    * @param listener The notification listener that was previously added to this emitter.
    * @throws ListenerNotFoundException If the listener is not registered with the emitter.
    * @see #addNotificationListener(NotificationListener, NotificationFilter, Object)
    */
   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException;
}
