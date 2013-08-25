/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.io.Serializable;

/**
 * Implemented by a class that wants to filter {@link Notification}s sent to a
 * {@link NotificationListener}.
 *
 * @version $Revision: 1.6 $
 */
public interface NotificationFilter extends Serializable
{
   /**
    * Invoked before sending the Notification to the listener.
    *
    * @return True if the Notification should be sent, false otherwise
    */
   public boolean isNotificationEnabled(Notification notification);
}
