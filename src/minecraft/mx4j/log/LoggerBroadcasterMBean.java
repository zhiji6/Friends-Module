/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.log;

import javax.management.ListenerNotFoundException;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 * Management interface for the LoggerBroadcaster MBean.
 *
 * @version $Revision: 1.5 $
 * @see LoggerBroadcaster
 */
public interface LoggerBroadcasterMBean
{
   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback);

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException;

   public void start();

   public void start(String category);

   public void stop();

   public void stop(String category);
}
