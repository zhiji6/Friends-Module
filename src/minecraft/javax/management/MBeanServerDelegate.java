/*
 * Copyright (C) The MX4J Contributors.
 * All rights reserved.
 *
 * This software is distributed under the terms of the MX4J License version 1.0.
 * See the terms of the MX4J License in the documentation provided with this software.
 */

package javax.management;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.rmi.server.UID;

/**
 * The MBean that broadcasts notifications about registration and unregistration of other MBeans.
 * It is registered with ObjectName <code>JMImplementation:type=MBeanServerDelegate</code>.
 * It also gives information about the JMX version and implementation.
 *
 * @version $Revision: 1.24 $
 */
public class MBeanServerDelegate implements MBeanServerDelegateMBean, NotificationEmitter
{
   private static long mbeanServerCount;
   // Only notifications of type MBeanServerNotification are emitted by this class, so the array must have length 1
   private static final MBeanNotificationInfo[] notifications = new MBeanNotificationInfo[]{new MBeanNotificationInfo
           (new String[]{MBeanServerNotification.REGISTRATION_NOTIFICATION, MBeanServerNotification.UNREGISTRATION_NOTIFICATION},
                   MBeanServerNotification.class.getName(), // as required by the spec
                   "Notifications emitted by the MBeanServerDelegate MBean upon registration or unregistration of MBeans")};

   private String mbeanServerID;
   private NotificationBroadcasterSupport broadcaster = new NotificationBroadcasterSupport();

   /**
    * Creates a new instance of the MBeanServerDelegate
    */
   public MBeanServerDelegate()
   {
   }

   public void addNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback) throws IllegalArgumentException
   {
      broadcaster.addNotificationListener(listener, filter, handback);
   }

   public void removeNotificationListener(NotificationListener listener) throws ListenerNotFoundException
   {
      broadcaster.removeNotificationListener(listener);
   }

   public void removeNotificationListener(NotificationListener listener, NotificationFilter filter, Object handback)
           throws ListenerNotFoundException
   {
      broadcaster.removeNotificationListener(listener, filter, handback);
   }

   public MBeanNotificationInfo[] getNotificationInfo()
   {
      return notifications;
   }

   /**
    * @see NotificationBroadcasterSupport#sendNotification
    */
   public void sendNotification(Notification notification)
   {
      broadcaster.sendNotification(notification);
   }

   public String getMBeanServerId()
   {
      // Evaluate lazily, since it may be an expensive operation
      synchronized (this)
      {
         if (mbeanServerID == null) mbeanServerID = generateMBeanServerID();
      }
      return mbeanServerID;
   }

   public String getImplementationName()
   {
      return "MX4J";
   }

   public String getImplementationVendor()
   {
      return "The MX4J Team";
   }

   public String getImplementationVersion()
   {
      return "3.0.2";
   }

   public String getSpecificationName()
   {
      return "Java Management Extensions";
   }

   public String getSpecificationVendor()
   {
      return "Sun Microsystems";
   }

   public String getSpecificationVersion()
   {
      return "1.2 Maintenance Release";
   }

   private String getLocalHost()
   {
      try
      {
         return InetAddress.getLocalHost().getHostName();
      }
      catch (UnknownHostException ignored)
      {
         return "localhost";
      }
   }

   private String generateMBeanServerID()
   {
      long count = 0;
      synchronized (MBeanServerDelegate.class)
      {
         ++mbeanServerCount;
         count = mbeanServerCount;
      }

      UID uid = new UID();
      return uid.toString() + ":" + getLocalHost() + ":" + count;
   }
}
