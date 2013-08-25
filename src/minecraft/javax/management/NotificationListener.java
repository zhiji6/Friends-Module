/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.util.EventListener;

/**
 * Implemented by an object that want to receive notifications.
 *
 * @version $Revision: 1.7 $
 */
public interface NotificationListener extends EventListener
{
   /**
    * Called when a notification occurs.
    *
    * @param notification The notification object
    * @param handback     Helps in associating information regarding the listener.
    */
   public void handleNotification(Notification notification, Object handback);
}
