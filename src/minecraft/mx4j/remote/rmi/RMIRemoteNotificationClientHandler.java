/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote.rmi;

import java.io.IOException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;

import javax.management.remote.NotificationResult;
import javax.management.remote.rmi.RMIConnection;

import mx4j.remote.AbstractRemoteNotificationClientHandler;
import mx4j.remote.ConnectionNotificationEmitter;
import mx4j.remote.HeartBeat;

/**
 * RMI-specific RemoteNotificationClientHandler.
 *
 * @version $Revision: 1.4 $
 */
public class RMIRemoteNotificationClientHandler extends AbstractRemoteNotificationClientHandler
{
   private final RMIConnection connection;
   private final ClassLoader defaultLoader;

   public RMIRemoteNotificationClientHandler(RMIConnection connection, ClassLoader defaultLoader, ConnectionNotificationEmitter emitter, HeartBeat heartbeat, Map environment)
   {
      super(emitter, heartbeat, environment);
      this.connection = connection;
      this.defaultLoader = defaultLoader;
   }

   protected NotificationResult fetchNotifications(long sequence, int maxNumber, long timeout) throws IOException
   {
      ClassLoader currentLoader = Thread.currentThread().getContextClassLoader();
      if (defaultLoader == null || defaultLoader.equals(currentLoader))
      {
         return invokeFetchNotifications(sequence, maxNumber, timeout);
      }
      else
      {
         try
         {
            setContextClassLoader(defaultLoader);
            return invokeFetchNotifications(sequence, maxNumber, timeout);
         }
         finally
         {
            setContextClassLoader(currentLoader);
         }
      }
   }

   private NotificationResult invokeFetchNotifications(long sequence, int maxNumber, long timeout) throws IOException
   {
      return connection.fetchNotifications(sequence, maxNumber, timeout);
   }

   private void setContextClassLoader(final ClassLoader loader)
   {
      AccessController.doPrivileged(new PrivilegedAction()
      {
         public Object run()
         {
            Thread.currentThread().setContextClassLoader(loader);
            return null;
         }
      });
   }
}
