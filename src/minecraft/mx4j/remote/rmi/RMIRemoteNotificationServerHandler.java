/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.util.ArrayList;
import java.util.Map;
import javax.management.remote.TargetedNotification;

import mx4j.log.Logger;
import mx4j.remote.DefaultRemoteNotificationServerHandler;
import mx4j.remote.MX4JRemoteUtils;

/**
 * @version $Revision: 1.5 $
 */
class RMIRemoteNotificationServerHandler extends DefaultRemoteNotificationServerHandler
{
   RMIRemoteNotificationServerHandler(Map environment)
   {
      super(environment);
   }

   protected TargetedNotification[] filterNotifications(TargetedNotification[] notifications)
   {
      Logger logger = null;
      ArrayList list = new ArrayList();
      for (int i = 0; i < notifications.length; ++i)
      {
         TargetedNotification notification = notifications[i];
         if (MX4JRemoteUtils.isTrulySerializable(notification))
         {
            list.add(notification);
         }
         else
         {
            if (logger == null) logger = getLogger();
            if (logger.isEnabledFor(Logger.INFO)) logger.info("Cannot send notification " + notification + " to the client: it is not serializable");
         }
      }
      return (TargetedNotification[])list.toArray(new TargetedNotification[list.size()]);
   }
}
