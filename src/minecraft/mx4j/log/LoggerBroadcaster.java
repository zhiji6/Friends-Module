/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package mx4j.log;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

/**
 * This broadcaster MBean allows to redirect MX4J internal logging to registered JMX listeners. <p>
 * Simply register this MBean in the MBeanServer, register one or more listener (eventually with filters
 * on the notification type), then call (directly or through MBeanServer) one of the <code>start</code>
 * operations. From this moment, MX4J internal logging is redirected to this MBean, and from here to all registered
 * listeners, basing on their filters.
 *
 * @version $Revision: 1.7 $
 */
public class LoggerBroadcaster extends NotificationBroadcasterSupport implements MBeanRegistration, LoggerBroadcasterMBean
{
   private long m_sequence;
   private boolean m_registered;
   private int m_recursionLevel;

   public ObjectName preRegister(MBeanServer server, ObjectName name) throws Exception
   {
      return name;
   }

   public void postRegister(Boolean registrationDone)
   {
      if (!registrationDone.booleanValue())
      {
         return;
      }
      m_registered = true;
   }

   public void preDeregister() throws Exception
   {
   }

   public void postDeregister()
   {
      m_registered = false;
   }

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws ListenerNotFoundException
   {
      // PENDING: for JMX 1.2 this has to be changed
      super.removeNotificationListener(listener);
   }

   public void sendNotification(Notification n)
   {
      // Since I use a broadcaster to log, it happens that log requests of the broadcaster itself are redirected
      // to the broadcaster, which broadcast to itself generating other log requests, an endless loop.
      // Here I stop reentrant calls: maxRecursionLevel == 1 means that initial log requests are broadcasted, but log
      // requests happening during this broadcast aren't; maxRecursionLevel == 2 means that initial log requests are
      // broadcasted, and also log requests happening during this broadcast, while log requests happening during
      // broadcast of log requests due to broacasting of the initial log request aren't.
      int maxRecursionLevel = 1;
      synchronized (this)
      {
         if (m_recursionLevel < maxRecursionLevel)
         {
            ++m_recursionLevel;
            super.sendNotification(n);
            --m_recursionLevel;
         }
      }
   }

   public void start()
   {
      Logger logger = createLoggerPrototype();
      Log.redirectTo(logger);
   }

   public void start(String category)
   {
      Logger logger = createLoggerPrototype();
      Log.redirectTo(logger, category);
   }

   public void stop()
   {
      Log.redirectTo(null);
   }

   public void stop(String category)
   {
      Log.redirectTo(null, category);
   }

   private boolean isRegistered()
   {
      return m_registered;
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      String[] types = new String[]{"mx4j.logger.trace",
                                    "mx4j.logger.debug",
                                    "mx4j.logger.info",
                                    "mx4j.logger.warn",
                                    "mx4j.logger.error",
                                    "mx4j.logger.fatal"};
      MBeanNotificationInfo notifs = new MBeanNotificationInfo(types, "javax.management.Notification", "MX4J Logger MBean notifications");
      return new MBeanNotificationInfo[]{notifs};
   }

   protected Logger createLoggerPrototype()
   {
      return new LoggerNotifier(this);
   }

   public static class LoggerNotifier extends Logger
   {
      private static LoggerBroadcaster m_loggerBroadcaster;

      private LoggerNotifier(LoggerBroadcaster mbean)
      {
         m_loggerBroadcaster = mbean;
      }

      public LoggerNotifier()
      {
      }

      protected void log(int priority, Object message, Throwable t)
      {
         // Notify listeners
         notify(priority, message, t);
      }

      private void notify(int priority, Object message, Throwable t)
      {
         if (m_loggerBroadcaster.isRegistered())
         {
            long sequence = 0;
            synchronized (this)
            {
               sequence = ++m_loggerBroadcaster.m_sequence;
            }

            String type = null;
            switch (priority)
            {
               case TRACE:
                  type = "mx4j.logger.trace";
                  break;
               case DEBUG:
                  type = "mx4j.logger.debug";
                  break;
               case INFO:
                  type = "mx4j.logger.info";
                  break;
               case WARN:
                  type = "mx4j.logger.warn";
                  break;
               case ERROR:
                  type = "mx4j.logger.error";
                  break;
               case FATAL:
                  type = "mx4j.logger.fatal";
                  break;
               default:
                  type = "mx4j.logger." + priority;
                  break;
            }

            String msg = message == null ? "" : message.toString();

            // TODO: the source must be the object name of the MBean if the listener was registered through MBeanServer
            Notification n = new Notification(type, this, sequence, msg);
            if (t != null)
            {
               n.setUserData(t);
            }

            m_loggerBroadcaster.sendNotification(n);
         }
      }
   }
}
