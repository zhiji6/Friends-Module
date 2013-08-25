/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

/**
 * Interface implemented by an MBean that emits Notifications.
 *
 * @version $Revision: 1.4 $
 * @see Notification
 * @since JMX 1.2
 */
public interface NotificationEmitter extends NotificationBroadcaster
{
   /**
    * Removes a notification listener from this MBean.
    * The MBean must have a registered listener that exactly matches the given listener, filter, and handback parameters.
    *
    * @param listener The listener that was previously added to this MBean.
    * @param filter   The filter that was specified when the listener was added.
    * @param handback The handback that was specified when the listener was added.
    * @throws ListenerNotFoundException If the triple listener, filter, handback is not registered with the emitter
    */
   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
           throws ListenerNotFoundException;
}
