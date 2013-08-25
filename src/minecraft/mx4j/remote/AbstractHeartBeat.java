/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.remote;

import java.io.IOException;
import java.util.Map;

/**
 * Base implementation for the HeartBeat interface.
 *
 * @version $Revision: 1.4 $
 */
public abstract class AbstractHeartBeat implements HeartBeat, Runnable
{
   private final ConnectionNotificationEmitter emitter;
   private long pulsePeriod;
   private int maxRetries;
   private Thread thread;
   private volatile boolean stopped;

   /**
    * Creates a new HeartBeat.
    *
    * @param emitter     The NotificationEmitter that sends connection failures notifications.
    * @param environment The environment that may contain properties that specify heart beat's behavior
    * @see #sendConnectionNotificationFailed
    * @see MX4JRemoteConstants#CONNECTION_HEARTBEAT_PERIOD
    * @see MX4JRemoteConstants#CONNECTION_HEARTBEAT_RETRIES
    */
   protected AbstractHeartBeat(ConnectionNotificationEmitter emitter, Map environment)
   {
      this.emitter = emitter;
      if (environment != null)
      {
         try
         {
            pulsePeriod = ((Long)environment.get(MX4JRemoteConstants.CONNECTION_HEARTBEAT_PERIOD)).longValue();
         }
         catch (Exception ignored)
         {
         }
         try
         {
            maxRetries = ((Integer)environment.get(MX4JRemoteConstants.CONNECTION_HEARTBEAT_RETRIES)).intValue();
         }
         catch (Exception ignored)
         {
         }
      }
      if (pulsePeriod <= 0) pulsePeriod = 5000;
      if (maxRetries <= 0) maxRetries = 3;
   }

   public long getPulsePeriod()
   {
      return pulsePeriod;
   }

   public int getMaxRetries()
   {
      return maxRetries;
   }

   /**
    * Subclasses will implement this method using protocol specific connections.
    * Normally the method {@link javax.management.MBeanServerConnection#getDefaultDomain} is used
    * to "ping" the server side.
    */
   protected abstract void pulse() throws IOException;

   public void start() throws IOException
   {
      thread = new Thread(this, "Connection HeartBeat");
      thread.setDaemon(true);
      thread.start();
   }

   public void stop() throws IOException
   {
      if (stopped) return;
      stopped = true;
      thread.interrupt();
   }

   public void run()
   {
      try
      {
         int retries = 0;
         while (!stopped && !thread.isInterrupted())
         {
            try
            {
               Thread.sleep(pulsePeriod);

               try
               {
                  pulse();
                  retries = 0;
               }
               catch (IOException x)
               {
                  if (retries++ == maxRetries)
                  {
                     // The connection has died
                     sendConnectionNotificationFailed();
                     break;
                  }
               }
            }
            catch (InterruptedException x)
            {
               Thread.currentThread().interrupt();
            }
         }
      }
      finally
      {
         stopped = true;
      }
   }

   /**
    * Sends the connection failed notification using the emitter specified in
    * {@link #AbstractHeartBeat}
    */
   protected void sendConnectionNotificationFailed()
   {
      emitter.sendConnectionNotificationFailed();
   }
}
